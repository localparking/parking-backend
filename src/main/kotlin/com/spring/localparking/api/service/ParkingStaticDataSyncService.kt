package com.spring.localparking.api.service

import com.spring.localparking.api.config.SeoulParkingApiClient
import com.spring.localparking.api.dto.ApiConstants
import com.spring.localparking.parking.domain.FeePolicy
import com.spring.localparking.parking.domain.ParkingLot
import com.spring.localparking.parking.repository.ParkingLotRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.system.measureTimeMillis

@Service
class ParkingStaticDataSyncService(
    private val seoulParkingApiClient: SeoulParkingApiClient,
    private val parkingLotRepository: ParkingLotRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

//    @PostConstruct
//    fun init() {
//        log.info("===== [SYSTEM] 애플리케이션 시작 시 주차장 기본 정보 초기화를 수행합니다. =====")
//        syncStaticParkingData()
//    }

    @Scheduled(cron = "0 0 4 * * SUN")
    @Transactional
    fun syncStaticParkingData() {
        log.info("==================================================")
        log.info("[BATCH START] 주차장 기본 정보 동기화를 시작합니다.")
        log.info("==================================================")
        val targetAreas = ApiConstants.SEOUL_API_AREAS

        val totalTime = measureTimeMillis {
            var totalProcessedCount = 0
            val existingParkingLots = parkingLotRepository.findAll().associateBy { it.parkingCode }

            targetAreas.forEachIndexed { index, areaName ->
                log.info("[PROCESSING {}/{}] '{}' 지역의 데이터를 가져옵니다...", index + 1, targetAreas.size, areaName)

                val parkingInfosForArea = seoulParkingApiClient.fetchDataForArea(areaName)
                if (parkingInfosForArea.isEmpty()) {
                    log.info(" -> '{}' 지역에 주차장 정보가 없습니다.", areaName)
                } else {
                    log.info(" -> '{}' 지역에서 {}건의 정보를 발견했습니다. DB 저장을 시작합니다.", areaName, parkingInfosForArea.size)
                    val parkingLotsToSave = mutableListOf<ParkingLot>()

                    parkingInfosForArea.forEach { info ->
                        val feePolicy = FeePolicy(
                            baseFee = info.baseFee?.toIntOrNull(),
                            baseTimeMin = info.baseTime?.toIntOrNull(),
                            additionalFee = info.additionalFee?.toIntOrNull(),
                            additionalTimeMin = info.additionalTime?.toIntOrNull()
                        )
                        val existingLot = existingParkingLots[info.parkingCode]

                        if (existingLot == null) {
                            val newParkingLot = ParkingLot.from(info, feePolicy)
                            parkingLotsToSave.add(newParkingLot)
                        } else {
                            existingLot.updateInfo(info, feePolicy)
                            parkingLotsToSave.add(existingLot)
                        }
                    }

                    if (parkingLotsToSave.isNotEmpty()) {
                        parkingLotRepository.saveAll(parkingLotsToSave)
                        totalProcessedCount += parkingLotsToSave.size
                    }
                }

                try {
                    if (index < targetAreas.size - 1) {
                        log.info(" -> 다음 API 호출 전 1초 대기합니다...")
                        Thread.sleep(1000)
                    }
                } catch (e: InterruptedException) {
                    log.error("Thread.sleep 중 오류 발생", e)
                }
            }
            log.info("[BATCH END] 총 저장/업데이트 건수: {}건", totalProcessedCount)
        }

        log.info("==================================================")
        log.info(" >> 총 소요 시간: {}초", String.format("%.2f", totalTime / 1000.0))
        log.info("==================================================")
    }
}