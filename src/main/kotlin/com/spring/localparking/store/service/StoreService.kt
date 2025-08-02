package com.spring.localparking.store.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.category.dto.CategoryDto
import com.spring.localparking.category.service.CategoryResolveService
import com.spring.localparking.search.dto.PageResponse
import com.spring.localparking.search.dto.PageSearchResponse
import com.spring.localparking.search.dto.PagingInfo
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.parking.domain.isOpened
import com.spring.localparking.parking.dto.AssociatedStoreDto
import com.spring.localparking.store.domain.Store
import com.spring.localparking.store.domain.StoreDocument
import com.spring.localparking.store.dto.*
import com.spring.localparking.store.repository.ProductRepository
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.store.repository.StoreSearchRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StoreService(
    private val storeSearchRepository: StoreSearchRepository,
    private val categoryResolveService: CategoryResolveService,
    private val storeRepository: StoreRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val productRepository: ProductRepository
) {
    private val PAGE_SIZE = 30

    fun search(req: StoreSearchRequest): PageResponse<StoreListResponse> {
        val resolvedCategoryIds = categoryResolveService.resolveAll(req.categoryIds)
        val pageable = PageRequest.of(req.page, PAGE_SIZE)
        val searchRequest = if (req.lat == null || req.lon == null) {
            req.copy(lat = 37.498095, lon = 127.027610)
        } else {
            req
        }
        val searchRadiusKm = if (searchRequest.distanceLevel == 2) 4 else 2
        val page = storeSearchRepository.searchByFilters(searchRequest, resolvedCategoryIds, pageable, searchRadiusKm)
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

    fun searchByText(req: StoreSearchRequest): PageSearchResponse<StoreListResponse> {
        if (req.query.isNullOrBlank()) {
            throw CustomException(ErrorCode.SEARCH_NOT_BLANK)
        }
        val expandedQuery = req.query?.takeIf { it.isNotBlank() }?.let {
            categoryResolveService.resolveCategoryNameToQuery(it)
        }
        val searchRequest = req.copy(query = expandedQuery)
        val resolvedCategoryIds = categoryResolveService.resolveAll(req.categoryIds)
        val pageable = PageRequest.of(req.page, PAGE_SIZE)

        val finalLat = req.lat ?: 37.498095
        val finalLon = req.lon ?: 127.027610

        var page: Page<StoreDocument>
        var searchRadiusKm = 2
        while (true) {
            page = storeSearchRepository.searchByText(searchRequest.copy(lat = finalLat, lon = finalLon), resolvedCategoryIds, pageable, searchRadiusKm)
            if (page.hasContent() || searchRadiusKm >= 10) {
                break
            }
            searchRadiusKm += 2
        }

        val content = page.content.map { StoreListResponse.of(it) }

        return PageSearchResponse(
            content = content,
            paging = PagingInfo(page = page.number, totalPages = page.totalPages),
            searchRadiusKm = searchRadiusKm
        )
    }
    fun getProductsByStore(storeId: Long): List<ProductResponseDto> {
        if (!storeRepository.existsById(storeId)) {
            throw CustomException(ErrorCode.STORE_NOT_FOUND)
        }
        val products = productRepository.findByStoreId(storeId)
        return products.map { product -> ProductResponseDto.from(product) }
    }
}