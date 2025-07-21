package com.spring.localparking.store.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.category.service.CategoryResolveService
import com.spring.localparking.global.dto.PageResponse
import com.spring.localparking.global.dto.PagingInfo
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.operatingHour.domain.isOpened
import com.spring.localparking.parking.dto.AssociatedStoreDto
import com.spring.localparking.store.domain.Store
import com.spring.localparking.store.dto.AssociatedParkingLotDto
import com.spring.localparking.store.dto.StoreDetailResponse
import com.spring.localparking.store.dto.StoreListResponse
import com.spring.localparking.store.dto.StoreSearchRequest
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.store.repository.StoreSearchRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StoreService(
    private val storeSearchRepository: StoreSearchRepository,
    private val categoryResolveService: CategoryResolveService,
    private val storeRepository: StoreRepository,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val PAGE_SIZE = 20

    fun search(req: StoreSearchRequest): PageResponse<StoreListResponse> {
        val categoryIds = categoryResolveService.resolveIds(req.categoryId)
        val pageable = PageRequest.of(req.page, PAGE_SIZE)
        val page = storeSearchRepository.searchByFilters(req, categoryIds, pageable)
        val content = page.content.map { StoreListResponse.of(it) }
        return PageResponse(
            content = content,
            paging = PagingInfo(page = page.number, totalPages = page.totalPages)
        )
    }

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
                        categoryNames = otherStore.categories.map { it.category.name },
                        storeName = otherStore.name,
                        isOpen = otherStore.operatingHour?.isOpened(LocalDateTime.now())
                    )
                }
            AssociatedParkingLotDto(
                parkingCode = lot.parkingCode,
                name = lot.name,
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
}
