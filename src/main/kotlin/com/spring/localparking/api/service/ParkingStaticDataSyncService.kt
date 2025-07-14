package com.spring.localparking.api.service

import com.spring.localparking.api.config.SeoulParkingApiClient
import com.spring.localparking.api.dto.ApiConstants
import com.spring.localparking.parking.domain.*
import com.spring.localparking.parking.repository.ParkingLotRepository
import com.spring.localparking.parking.repository.ParkingLotSearchRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.geo.GeoPoint
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis

@Service
class ParkingStaticDataSyncService(
    private val seoulParkingApiClient: SeoulParkingApiClient,
    private val parkingLotRepository: ParkingLotRepository,
    private val parkingLotSearchRepository: ParkingLotSearchRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

//    @PostConstruct
//    fun init() {
//        log.info("===== [SYSTEM] 애플리케이션 시작 시 주차장 기본 정보 초기화를 수행합니다. =====")
//        syncStaticParkingData()
//    }

    @Scheduled(cron = "0 0 2 * * MON")
    @Transactional
    fun syncStaticParkingData() {
        log.info("==================================================")
        log.info("[BATCH START] 주차장 기본 정보 동기화를 시작합니다.")
        log.info("==================================================")
        val targetAreas = ApiConstants.SEOUL_API_AREAS

        val totalTime = measureTimeMillis {
            var totalProcessedCount = 0
            val existingParkingLots = parkingLotRepository.findAll().associateBy { it.parkingCode }
            val parkingLotsToSave = mutableListOf<ParkingLot>()

            targetAreas.forEachIndexed { index, areaName ->
                log.info("[PROCESSING {}/{}] '{}' 지역의 데이터를 가져옵니다...", index + 1, targetAreas.size, areaName)

                val parkingInfosForArea = seoulParkingApiClient.fetchParkingDataForHotspot(areaName)
                if (parkingInfosForArea.isNotEmpty()) {
                    log.info(" -> '{}' 지역에서 {}건의 정보를 발견했습니다.", areaName, parkingInfosForArea.size)

                    parkingInfosForArea.forEach { info ->
                        val feePolicy = FeePolicy(
                            baseFee = info.baseFee?.toIntOrNull(),
                            baseTimeMin = info.baseTime?.toIntOrNull(),
                            additionalFee = info.additionalFee?.toIntOrNull(),
                            additionalTimeMin = info.additionalTime?.toIntOrNull()
                        )
                        val operatingHour = OperatingHour()
                        val weekdayBegin = parseTimeToLocalTime(info.weekdayBeginTime)
                        val weekdayEnd = parseTimeToLocalTime(info.weekdayEndTime)
                        if (weekdayBegin != null && weekdayEnd != null) {
                            val weekdays = listOf(
                                DayOfWeek.MONDAY,
                                DayOfWeek.TUESDAY,
                                DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY,
                                DayOfWeek.FRIDAY
                            )
                            weekdays.forEach { day ->
                                operatingHour.addTimeSlot(
                                    TimeSlot(
                                        dayOfWeek = day,
                                        beginTime = weekdayBegin,
                                        endTime = weekdayEnd
                                    )
                                )
                            }
                        }
                        val weekendBegin = parseTimeToLocalTime(info.weekendBeginTime)
                        val weekendEnd = parseTimeToLocalTime(info.weekendEndTime)
                        if (weekendBegin != null && weekendEnd != null) {
                            operatingHour.addTimeSlot(
                                TimeSlot(
                                    dayOfWeek = DayOfWeek.SATURDAY,
                                    beginTime = weekendBegin,
                                    endTime = weekendEnd
                                )
                            )
                            operatingHour.addTimeSlot(
                                TimeSlot(
                                    dayOfWeek = DayOfWeek.SUNDAY,
                                    beginTime = weekendBegin,
                                    endTime = weekendEnd
                                )
                            )
                        }
                        val holidayBegin = parseTimeToLocalTime(info.holidayBeginTime)
                        val holidayEnd = parseTimeToLocalTime(info.holidayEndTime)

                        if (holidayBegin != null && holidayEnd != null) {
                            operatingHour.addTimeSlot(TimeSlot(dayOfWeek = DayOfWeek.SUNDAY, beginTime = holidayBegin, endTime = holidayEnd))
                        }

                        val existingLot = existingParkingLots[info.parkingCode]
                        if (existingLot == null) {
                            parkingLotsToSave.add(ParkingLot.from(info, feePolicy, operatingHour))
                        } else {
                            existingLot.updateInfo(info, feePolicy, operatingHour)
                            parkingLotsToSave.add(existingLot)
                        }
                    }
                }

                try {
                    if (index < targetAreas.size - 1) {
                        log.info(" -> 다음 API 호출 전 1초 대기합니다...")
                        Thread.sleep(1000)
                    }
                } catch (e: InterruptedException) {
                    log.error("Thread.sleep 중 오류 발생", e)
                    Thread.currentThread().interrupt()
                }
            }

            if (parkingLotsToSave.isNotEmpty()) {
                parkingLotRepository.saveAll(parkingLotsToSave)
                totalProcessedCount = parkingLotsToSave.size
                log.info(" -> 총 {}건의 데이터를 DB에 저장했습니다.", totalProcessedCount)

                val documents = parkingLotsToSave
                    .filter { it.lat != null && it.lon != null }
                    .map { lot ->
                        val operatingHoursForDoc = lot.operatingHour?.timeSlots?.map { ts ->
                            val beginTimeStr = ts.beginTime.format(DateTimeFormatter.ofPattern("HHmm"))
                            val endTimeStr = ts.endTime.format(DateTimeFormatter.ofPattern("HHmm"))
                            DocumentOperatingHour(
                                dayOfWeek = ts.dayOfWeek,
                                beginTime = beginTimeStr.toIntOrNull(),
                                endTime = endTimeStr.toIntOrNull(),
                                isOvernight = ts.beginTime.isAfter(ts.endTime)
                            )
                        } ?: listOf()
                        val is24Hours = operatingHoursForDoc.any {
                            (it.beginTime == 0 && it.endTime == 2400) || (it.beginTime == 0 && it.endTime == 2359)
                        }
                        ParkingLotDocument(
                            parkingCode = lot.parkingCode,
                            name = lot.name,
                            address = lot.address,
                            location = GeoPoint(lot.lat!!, lot.lon!!),
                            isFree = lot.isFree,
                            isRealtime = lot.isRealtime,
                            is24Hours = is24Hours,
                            capacity = lot.capacity,
                            baseFee = lot.feePolicy?.baseFee,
                            baseTimeMin = lot.feePolicy?.baseTimeMin,
                            hourlyFee = calculateHourlyFee(lot.feePolicy),
                            operatingHours = operatingHoursForDoc
                        )
                    }

                if (documents.isNotEmpty()) {
                    parkingLotSearchRepository.saveAll(documents)
                    log.info(" -> {}건의 데이터를 Elasticsearch에 동기화했습니다.", documents.size)
                }
            }
            log.info("[BATCH END] 총 저장/업데이트 건수: {}건", totalProcessedCount)
        }
        log.info(" >> 총 소요 시간: {}초", String.format("%.2f", totalTime / 1000.0))
        log.info("==================================================")
    }

    private fun calculateHourlyFee(feePolicy: FeePolicy?): Int? {
        if (feePolicy?.baseFee == null || feePolicy.baseTimeMin == null) {
            return null
        }
        if (feePolicy.baseTimeMin!! >= 60) {
            return feePolicy.baseFee
        }

        val additionalFee = feePolicy.additionalFee ?: 0
        val additionalTimeMin = feePolicy.additionalTimeMin ?: 10

        val remainingTime = 60 - feePolicy.baseTimeMin!!
        val additionalChunks = (remainingTime + additionalTimeMin - 1) / additionalTimeMin

        return feePolicy.baseFee!! + (additionalChunks * additionalFee)
    }
    private fun parseTimeToLocalTime(timeStr: String?): LocalTime? {
        if (timeStr.isNullOrBlank() || timeStr.length != 4) return null
        if (timeStr == "2400") return LocalTime.MAX

        return try {
            val hour = timeStr.substring(0, 2).toInt()
            val minute = timeStr.substring(2, 4).toInt()
            LocalTime.of(hour, minute)
        } catch (e: Exception) {
            null
        }
    }
}