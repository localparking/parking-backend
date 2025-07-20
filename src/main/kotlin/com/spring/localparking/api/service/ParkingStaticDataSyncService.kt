package com.spring.localparking.api.service

import com.spring.localparking.api.config.SeoulParkingApiClient
import com.spring.localparking.api.dto.ParkingInfo
import com.spring.localparking.api.dto.ApiConstants
import com.spring.localparking.operatingHour.domain.*
import com.spring.localparking.parking.domain.FeePolicy
import com.spring.localparking.parking.domain.ParkingLot
import com.spring.localparking.parking.domain.ParkingLotDocument
import com.spring.localparking.parking.repository.ParkingLotRepository
import com.spring.localparking.parking.repository.ParkingLotSearchRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.geo.GeoPoint
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.*
import kotlin.system.measureTimeMillis

@Service
class ParkingStaticDataSyncService(
    private val seoulParkingApiClient: SeoulParkingApiClient,
    private val parkingLotRepository: ParkingLotRepository,
    private val parkingLotSearchRepository: ParkingLotSearchRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val ZONE: ZoneId = ZoneId.of("Asia/Seoul")

    @PostConstruct
    fun init() {
        log.info("===== [SYSTEM] 애플리케이션 시작: 주차장 기본 정보 초기화 실행 =====")
        syncStaticParkingData()
    }

    @Scheduled(cron = "0 0 2 * * MON")
    @Transactional
    fun syncStaticParkingData() {
        log.info("==================================================")
        log.info("[BATCH START] 주차장 기본 정보 동기화 시작")
        log.info("==================================================")

        val batchStart = LocalDateTime.now(ZONE)
        val todayDow = batchStart.dayOfWeek

        val existingMap = parkingLotRepository.findAll().associateBy { it.parkingCode }
        val toPersist = mutableListOf<ParkingLot>()

        val totalMillis = measureTimeMillis {
            ApiConstants.SEOUL_API_AREAS.forEachIndexed { idx, area ->
                log.info("[{}/{}] '{}' 지역 데이터 수집...", idx + 1, ApiConstants.SEOUL_API_AREAS.size, area)
                val infos = seoulParkingApiClient.fetchParkingDataForHotspot(area)

                if (infos.isEmpty()) {
                    log.info(" -> '{}' 지역: 수신 0건", area)
                } else {
                    log.info(" -> '{}' 지역: {}건 수신", area, infos.size)
                }

                infos.forEach { info ->
                    val feePolicy = buildFeePolicy(info)
                    val op = buildOperatingHour(info)

                    val lot = existingMap[info.parkingCode]?.apply {
                        updateInfo(info, feePolicy, op)
                    } ?: ParkingLot.from(info, feePolicy, op)

                    toPersist += lot
                }

                // API 호출 rate 제한 완충
                if (idx < ApiConstants.SEOUL_API_AREAS.size - 1) {
                    try {
                        Thread.sleep(1000)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        log.warn("Sleep interrupted", ie)
                    }
                }
            }

            // ---------- RDB 저장 ----------
            if (toPersist.isNotEmpty()) {
                parkingLotRepository.saveAll(toPersist)
                log.info("DB 저장/업데이트: {}건", toPersist.size)
            } else {
                log.info("저장할 변경 사항이 없습니다.")
            }

            // ---------- ES 문서 구성 ----------
            val documents = toPersist
                .asSequence()
                .filter { it.lat != null && it.lon != null }
                .map { lot ->
                    val op = lot.operatingHour
                    val isOpenNow: Boolean? = op?.isOpenAt(batchStart)
                    val is24Today: Boolean = op?.is24Hours(batchStart.dayOfWeek) ?: false

                    val docHours = op?.timeSlots?.map {
                        DocumentOperatingHour(
                            dayOfWeek = it.dayOfWeek,
                            beginTime = it.beginTime.hour * 100 + it.beginTime.minute,
                            endTime = it.endTime.hour * 100 + it.endTime.minute,
                            isOvernight = it.isOvernight()
                        )
                    } ?: emptyList()

                    ParkingLotDocument(
                        parkingCode = lot.parkingCode,
                        name = lot.name,
                        address = lot.address,
                        location = GeoPoint(lot.lat!!, lot.lon!!),
                        isFree = lot.isFree,
                        isRealtime = lot.isRealtime,
                        isOpen = isOpenNow,
                        is24Hours = is24Today,
                        congestion = null,
                        capacity = lot.capacity,
                        baseFee = lot.feePolicy?.baseFee,
                        baseTimeMin = lot.feePolicy?.baseTimeMin,
                        hourlyFee = calculateHourlyFee(lot.feePolicy),
                        operatingHours = docHours
                    )
                }.toList()

            if (documents.isNotEmpty()) {
                parkingLotSearchRepository.saveAll(documents)
                log.info("ES 색인: {}건", documents.size)
            } else {
                log.info("색인 대상 문서 없음 (위치 정보 없는 항목 제외됨)")
            }
        }

        log.info("[BATCH END] 총 소요: {}초", "%.2f".format(totalMillis / 1000.0))
        log.info("==================================================")
    }

    private fun buildFeePolicy(info: ParkingInfo): FeePolicy =
        FeePolicy(
            baseFee = info.baseFee?.toIntOrNull(),
            baseTimeMin = info.baseTime?.toIntOrNull(),
            additionalFee = info.additionalFee?.toIntOrNull(),
            additionalTimeMin = info.additionalTime?.toIntOrNull()
        )

    private fun buildOperatingHour(info: ParkingInfo): OperatingHour {
        val op = OperatingHour()

        // 평일
        addWeekdaySlots(info, op)
        // 주말(토/일)
        addWeekendSlots(info, op)
        // 공휴일 (현재 Sunday 중복 가능성 -> 동일 시간 중복 방지)
        addHolidaySlot(info, op)

        return op
    }

    private fun addWeekdaySlots(info: ParkingInfo, op: OperatingHour) {
        val b = parseTime(info.weekdayBeginTime)
        val e = parseTime(info.weekdayEndTime)
        if (b != null && e != null) {
            listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            ).forEach { op.addTimeSlot(TimeSlot(dayOfWeek = it, beginTime = b, endTime = e)) }
        }
    }

    private fun addWeekendSlots(info: ParkingInfo, op: OperatingHour) {
        val b = parseTime(info.weekendBeginTime)
        val e = parseTime(info.weekendEndTime)
        if (b != null && e != null) {
            op.addTimeSlot(TimeSlot(dayOfWeek = DayOfWeek.SATURDAY, beginTime = b, endTime = e))
            op.addTimeSlot(TimeSlot(dayOfWeek = DayOfWeek.SUNDAY, beginTime = b, endTime = e))
        }
    }

    private fun addHolidaySlot(info: ParkingInfo, op: OperatingHour) {
        val b = parseTime(info.holidayBeginTime)
        val e = parseTime(info.holidayEndTime)
        if (b != null && e != null) {
            val dup = op.timeSlots.any {
                it.dayOfWeek == DayOfWeek.SUNDAY && it.beginTime == b && it.endTime == e
            }
            if (!dup) {
                op.addTimeSlot(TimeSlot(dayOfWeek = DayOfWeek.SUNDAY, beginTime = b, endTime = e))
            }
        }
    }

    private fun parseTime(raw: String?): LocalTime? {
        if (raw.isNullOrBlank() || raw.length != 4) return null
        if (raw == "2400") return LocalTime.MAX
        return try {
            val h = raw.substring(0, 2).toInt()
            val m = raw.substring(2, 4).toInt()
            LocalTime.of(h, m)
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateHourlyFee(feePolicy: FeePolicy?): Int? {
        val baseFee = feePolicy?.baseFee
        val baseTime = feePolicy?.baseTimeMin
        if (baseFee == null || baseTime == null) return null

        if (baseTime >= 60) return baseFee

        val addFee = feePolicy.additionalFee ?: 0
        val addTime = feePolicy.additionalTimeMin ?: 10
        val remaining = 60 - baseTime
        val chunks = (remaining + addTime - 1) / addTime
        return baseFee + chunks * addFee
    }
}
