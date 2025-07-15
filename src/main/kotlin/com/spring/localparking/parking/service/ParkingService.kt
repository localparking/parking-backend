package com.spring.localparking.parking.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.global.dto.PageResponse
import com.spring.localparking.global.dto.PagingInfo
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.parking.dto.AssociatedStoreDto
import com.spring.localparking.parking.dto.ParkingLotDetailResponse
import com.spring.localparking.parking.dto.ParkingLotListResponse
import com.spring.localparking.parking.dto.ParkingLotSearchRequest
import com.spring.localparking.parking.repository.ParkingLotRepository
import com.spring.localparking.parking.repository.ParkingLotSearchRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class ParkingLotService(
    private val parkingLotRepository: ParkingLotRepository,
    private val parkingLotSearchRepository: ParkingLotSearchRepository,
    private val redisTemplate: StringRedisTemplate
) {
    private val PAGE_SIZE = 20

    fun search(request: ParkingLotSearchRequest): PageResponse<ParkingLotListResponse> {
        val pageable = PageRequest.of(request.page, PAGE_SIZE)

        // 1. Elasticsearch에서 모든 조건에 맞는 주차장 검색
        val pageResult = parkingLotSearchRepository.searchByFilters(request, pageable)
        val documents = pageResult.content

        // 2. Redis에서 실시간 정보 조회
        val parkingCodes = documents.map { it.parkingCode }
        val realtimeInfoMap = getRealtimeInfo(parkingCodes)

        // 3. 데이터 조합
        val content = documents.map { doc ->
            val realtimeInfo = realtimeInfoMap[doc.parkingCode]
            val curCapacity = realtimeInfo?.second
            val isOpen = isParkingLotOpenNow(doc.parkingCode)
            ParkingLotListResponse.of(doc, curCapacity, isOpen)
        }

        val pagingInfo = PagingInfo(page = pageResult.number, totalPages = pageResult.totalPages)
        return PageResponse(content, pagingInfo)
    }


    private fun isParkingLotOpenNow(parkingCode: String): Boolean? {
        val parkingLot = parkingLotRepository.findById(parkingCode).orElse(null)

        if (parkingLot == null) {
            return null
        }

        return parkingLot.operatingHour?.isOpenNow()
    }

    fun getDetail(parkingCode: String): ParkingLotDetailResponse {
        val parkingLot = parkingLotRepository.findById(parkingCode)
            .orElseThrow { CustomException(ErrorCode.PARKING_LOT_NOT_FOUND) }

        val realtimeInfo = getRealtimeInfo(listOf(parkingCode))[parkingCode]
        val congestion = realtimeInfo?.first
        val curCapacity = realtimeInfo?.second

        val isOpen = isParkingLotOpenNow(parkingCode)

        val mockStores = listOf(
            AssociatedStoreDto(storeId = 1L, categoryName = "한식", storeName = "강남 할머니 칼국수", isOpen = true),
            AssociatedStoreDto(storeId = 2L, categoryName = "카페", storeName = "스타벅스 강남역점", isOpen = false),
            AssociatedStoreDto(storeId = 3L, categoryName = "일식", storeName = "호랑이 초밥", isOpen = null)
        )

        return ParkingLotDetailResponse.from(parkingLot, congestion, curCapacity, isOpen, mockStores)
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
}