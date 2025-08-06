package com.spring.localparking.parking.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.category.dto.CategoryDto
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.parking.domain.isOpened
import com.spring.localparking.parking.dto.*
import com.spring.localparking.parking.repository.ParkingLotRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ParkingLotService(
    private val parkingLotRepository: ParkingLotRepository,
    private val redisTemplate: StringRedisTemplate
) {
    fun getDetail(parkingCode: String): ParkingLotDetailResponse {
        val parkingLot = parkingLotRepository.findById(parkingCode)
            .orElseThrow { CustomException(ErrorCode.PARKING_LOT_NOT_FOUND) }

        val realtimeInfo = getRealtimeInfo(listOf(parkingCode))[parkingCode]
        val congestion = realtimeInfo?.first
        val curCapacity = realtimeInfo?.second

        val associatedStores = parkingLot.storeParkingLots.map { sp ->
            val otherStore = sp.store
            AssociatedStoreDto(
                storeId = otherStore.id,
                categories = otherStore.categories.map { storeCategory ->
                    CategoryDto(
                        categoryId = storeCategory.category.id,
                        categoryName = storeCategory.category.name,
                        parentId = storeCategory.category.parent?.id
                    )
                },
                storeName = otherStore.name,
                isOpen = otherStore.operatingHour?.isOpened(LocalDateTime.now())
            )
        }

        return ParkingLotDetailResponse.from(parkingLot, congestion, curCapacity, associatedStores)
    }

    fun getRealtimeInfo(parkingCodes: List<String>): Map<String, Pair<String?, Int?>> {
        if (parkingCodes.isEmpty()) return emptyMap()

        val results = redisTemplate.executePipelined { connection ->
            parkingCodes.forEach { code ->
                val key = "parking:realtime:$code".toByteArray()
                connection.hashCommands().hMGet(key, "status".toByteArray(), "curCapacity".toByteArray())
            }
            null
        }

        return parkingCodes.zip(results).associate { (code, result) ->
            val values = result as? List<ByteArray?> ?: listOf(null, null)
            val status = values.getOrNull(0)?.let { String(it) }
            val availableSpaces = values.getOrNull(1)?.let { String(it).toIntOrNull() }
            code to Pair(status, availableSpaces)
        }
    }
}