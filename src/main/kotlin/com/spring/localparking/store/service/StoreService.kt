package com.spring.localparking.store.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.category.dto.CategoryDto
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.parking.domain.isOpened
import com.spring.localparking.parking.dto.AssociatedStoreDto
import com.spring.localparking.store.domain.Store
import com.spring.localparking.store.dto.*
import com.spring.localparking.store.repository.ProductRepository
import com.spring.localparking.store.repository.StoreRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StoreService(
    private val storeRepository: StoreRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val productRepository: ProductRepository
) {
    fun getDetail(storeId: Long): StoreDetailResponse {
        val store: Store = storeRepository.findWithParkingLotsById(storeId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        val associatedParkingLots = store.storeParkingLots.map { sp ->
            val lot = sp.parkingLot
            val otherStores = lot.storeParkingLots
                .filter { otherSp -> otherSp.store.id != store.id }
                .map { otherSp ->
                    val otherStore = otherSp.store
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
            AssociatedParkingLotDto(
                parkingCode = lot.parkingCode,
                name = lot.name,
                isOpen = lot.operatingHour?.isOpened(LocalDateTime.now()),
                hourlyFee = lot.hourlyFee,
                capacity = lot.capacity,
                curCapacity = getRealtimeInfo(listOf(lot.parkingCode))[lot.parkingCode],
                otherStores = otherStores
            )
        }
        return StoreDetailResponse.from(
            store,
            associatedParkingLots
        )
    }

    private fun getRealtimeInfo(parkingCodes: List<String>): Map<String, Int?> {
        if (parkingCodes.isEmpty()) return emptyMap()

        val results = redisTemplate.executePipelined { conn ->
            parkingCodes.forEach { code ->
                conn.hashCommands().hMGet("parking:realtime:$code".toByteArray(), "curCapacity".toByteArray())
            }
            null
        }

        return parkingCodes.zip(results).associate { (code, res) ->
            val list = res as? List<ByteArray?> ?: listOf(null)
            val cur = list.getOrNull(0)?.let { String(it).toIntOrNull() }
            code to cur
        }
    }

    fun getProductsByStore(storeId: Long): List<ProductResponseDto> {
        if (!storeRepository.existsById(storeId)) {
            throw CustomException(ErrorCode.STORE_NOT_FOUND)
        }
        val products = productRepository.findByStoreId(storeId)
        return products.map { product -> ProductResponseDto.from(product) }
    }
}