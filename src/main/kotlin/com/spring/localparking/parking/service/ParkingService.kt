package com.spring.localparking.parking.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.category.dto.CategoryDto
import com.spring.localparking.search.dto.PageResponse
import com.spring.localparking.search.dto.PageSearchResponse
import com.spring.localparking.search.dto.PagingInfo
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.parking.domain.ParkingLotDocument
import com.spring.localparking.parking.domain.isOpened
import com.spring.localparking.parking.dto.*
import com.spring.localparking.parking.repository.ParkingLotRepository
import com.spring.localparking.parking.repository.ParkingLotSearchRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ParkingLotService(
    private val parkingLotRepository: ParkingLotRepository,
    private val parkingLotSearchRepository: ParkingLotSearchRepository,
    private val redisTemplate: StringRedisTemplate
) {
    private val PAGE_SIZE = 20

    fun search(req: ParkingLotSearchRequest): PageResponse<ParkingLotListResponse> {
        val pageable = PageRequest.of(req.page, PAGE_SIZE)
        val searchRequest = if (req.lat == null || req.lon == null) {
            req.copy(lat = 37.498095, lon = 127.027610)
        } else {
            req
        }
        // 1. Elasticsearch에서 모든 조건에 맞는 주차장 검색
        val pageResult = parkingLotSearchRepository.searchByFilters(searchRequest, pageable)
        val documents = pageResult.content

        // 2. Redis에서 실시간 정보 조회
        val parkingCodes = documents.map { it.parkingCode }
        val realtimeInfoMap = getRealtimeInfo(parkingCodes)

        // 3. 데이터 조합
        val content = documents.map { doc ->
            val realtimeInfo = realtimeInfoMap[doc.parkingCode]
            val curCapacity = realtimeInfo?.second
            ParkingLotListResponse.of(doc, curCapacity)
        }
        val pagingInfo = PagingInfo(page = pageResult.number, totalPages = pageResult.totalPages)
        return PageResponse(content, pagingInfo)
    }


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

    private fun getRealtimeInfo(parkingCodes: List<String>): Map<String, Pair<String?, Int?>> {
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
    fun searchByText(req: ParkingLotSearchRequest): PageSearchResponse<ParkingLotListResponse> {
        if (req.query.isNullOrBlank()) {
            throw CustomException(ErrorCode.SEARCH_NOT_BLANK)
        }
        val pageable = PageRequest.of(req.page, PAGE_SIZE)
        var searchRadiusKm: Int
        val page: Page<ParkingLotDocument>

        if (req.lat != null && req.lon != null) {
            searchRadiusKm = 2
            var initialPage = parkingLotSearchRepository.searchByText(req.copy(lat = req.lat, lon = req.lon), pageable)

            if (initialPage.isEmpty) {
                searchRadiusKm = 4
                initialPage = parkingLotSearchRepository.searchByText(req.copy(lat = req.lat, lon = req.lon), pageable)
            }
            page = initialPage
        } else {
            searchRadiusKm = 3
            page = parkingLotSearchRepository.searchByText(req.copy(lat = 37.498095, lon = 127.027610), pageable)
        }

        val documents = page.content
        val parkingCodes = documents.map { it.parkingCode }
        val realtimeInfoMap = getRealtimeInfo(parkingCodes)

        val content = documents.map { doc ->
            val realtimeInfo = realtimeInfoMap[doc.parkingCode]
            ParkingLotListResponse.of(doc, realtimeInfo?.second)
        }

        return PageSearchResponse(
            content,
            PagingInfo(page = page.number, totalPages = page.totalPages),
            searchRadiusKm
        )
    }
}