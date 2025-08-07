package com.spring.localparking.api.service

import com.spring.localparking.api.config.SeoulParkingApiClient
import com.spring.localparking.api.dto.ApiConstants
import com.spring.localparking.search.repository.parking.ParkingLotSearchRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis

@Service
class ParkingRealtimeDataSyncService(
    private val seoulParkingApiClient: SeoulParkingApiClient,
    private val redisTemplate: StringRedisTemplate,
    private val parkingLotSearchRepository: ParkingLotSearchRepository
) {
    //@Scheduled(fixedRate = 60000)
    fun syncRealtimeParkingData() {
        var totalUpdatedCount = 0

        val totalTime = measureTimeMillis {
            redisTemplate.executePipelined { connection ->
                ApiConstants.SEOUL_API_AREAS.forEach { areaName ->
                    val parkingInfosForArea = seoulParkingApiClient.fetchParkingDataForHotspot(areaName)

                    val targetInfos = parkingInfosForArea.filter { it.isRealtimeEnabled}

                    targetInfos.forEach { info ->
                        val curCapacity = info.currentParkingCount?.toIntOrNull()
                        val totalCapacity = info.capacity?.toIntOrNull()

                        if (curCapacity != null && totalCapacity != null) {
                            val status = calculateParkingStatus(curCapacity, totalCapacity)
                            val key = "parking:realtime:${info.parkingCode}"
                            val values = mapOf(
                                "curCapacity" to curCapacity.toString(),
                                "status" to status,
                                "lastUpdated" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            )

                            val rawKey = redisTemplate.stringSerializer.serialize(key)
                            values.forEach { (field, value) ->
                                val rawField = redisTemplate.stringSerializer.serialize(field)
                                val rawValue = redisTemplate.stringSerializer.serialize(value)
                                if (rawKey != null && rawField != null && rawValue != null) {
                                    connection.hashCommands().hSet(rawKey, rawField, rawValue)
                                }
                            }
                            parkingLotSearchRepository.findById(info.parkingCode).ifPresent { doc ->
                                val updatedDoc = doc.copy(congestion = status)
                                parkingLotSearchRepository.save(updatedDoc)
                            }
                            totalUpdatedCount++
                        }
                    }
                }
                null
            }
        }
    }

    private fun calculateParkingStatus(availableSpaces: Int, totalCapacity: Int): String {
        if (totalCapacity <= 0) {
            return "정보없음"
        }
        val availabilityRatio = availableSpaces.toDouble() / totalCapacity.toDouble()
        return when {
            availabilityRatio > 0.3 -> "여유"
            availabilityRatio > 0.1 -> "보통"
            else -> "혼잡"
        }
    }
}