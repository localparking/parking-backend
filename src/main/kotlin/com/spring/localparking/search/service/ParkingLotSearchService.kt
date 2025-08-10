package com.spring.localparking.search.service


import com.spring.global.exception.ErrorCode
import com.spring.localparking.search.dto.page.PageResponse
import com.spring.localparking.search.dto.page.PageSearchResponse
import com.spring.localparking.search.dto.page.PagingInfo
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.search.domain.ParkingLotDocument
import com.spring.localparking.parking.service.ParkingLotService
import com.spring.localparking.search.dto.ParkingLotListResponse
import com.spring.localparking.search.dto.ParkingLotSearchRequest
import com.spring.localparking.search.dto.ParkingSimpleResponse
import com.spring.localparking.search.repository.parking.ParkingLotSearchRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ParkingLotSearchService(
    private val parkingService: ParkingLotService,
    private val parkingLotSearchRepository: ParkingLotSearchRepository
) {
    private val PAGE_SIZE = 30

    fun search(req: ParkingLotSearchRequest): PageResponse<ParkingLotListResponse> {
        val pageable = PageRequest.of(req.page, PAGE_SIZE)
        val searchRequest = if (req.lat == null || req.lon == null) {
            req.copy(lat = 37.498095, lon = 127.027610)
        } else {
            req
        }
        val searchRadiusKm = if (searchRequest.distanceLevel == 2) 4 else 2
        // 1. Elasticsearch에서 모든 조건에 맞는 주차장 검색
        val pageResult = parkingLotSearchRepository.searchByFilters(searchRequest, pageable, searchRadiusKm)
        val documents = pageResult.content

        // 2. Redis에서 실시간 정보 조회
        val parkingCodes = documents.map { it.parkingCode }
        val realtimeInfoMap = parkingService.getRealtimeInfo(parkingCodes)

        // 3. 데이터 조합
        val content = documents.map { doc ->
            val realtimeInfo = realtimeInfoMap[doc.parkingCode]
            val curCapacity = realtimeInfo?.second
            ParkingLotListResponse.of(doc, curCapacity)
        }
        val pagingInfo = PagingInfo(page = pageResult.number, totalPages = pageResult.totalPages)
        return PageResponse(content, pagingInfo)
    }

    fun searchByText(req: ParkingLotSearchRequest): PageSearchResponse<ParkingLotListResponse> {
        if (req.query.isNullOrBlank()) {
            throw CustomException(ErrorCode.SEARCH_NOT_BLANK)
        }
        val pageable = PageRequest.of(req.page, PAGE_SIZE)
        val searchRequest = if (req.lat == null || req.lon == null) {
            req.copy(lat = 37.498095, lon = 127.027610)
        } else {
            req
        }

        var page: Page<ParkingLotDocument>
        var searchRadiusKm = 2
        while (true) {
            page = parkingLotSearchRepository.searchByText(searchRequest, pageable, searchRadiusKm)
            if (page.hasContent() || searchRadiusKm >= 10) {
                break
            }
            searchRadiusKm += 2
        }

        val documents = page.content
        val parkingCodes = documents.map { it.parkingCode }
        val realtimeInfoMap = parkingService.getRealtimeInfo(parkingCodes)

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
    fun searchByNameForRegistration(query: String): List<ParkingSimpleResponse> {
        if (query.isBlank()) {
            return emptyList()
        }
        val documents = parkingLotSearchRepository.searchByName(query)
        return documents.map { doc ->
            ParkingSimpleResponse(
                parkingCode = doc.parkingCode,
                name = doc.name,
                address = doc.address
            )
        }
    }
}