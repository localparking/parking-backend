package com.spring.localparking.search.controller

import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.search.dto.ParkingLotListResponse
import com.spring.localparking.search.dto.ParkingLotSearchRequest
import com.spring.localparking.search.dto.page.PageResponse
import com.spring.localparking.search.dto.page.PageSearchResponse
import com.spring.localparking.search.service.ParkingLotSearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "주차장 컨트롤러", description = "주차장 관련 API입니다.")
@RestController
@RequestMapping("/parking")
class ParkingSearchController(
    private val parkingLotSearchService: ParkingLotSearchService
) {
    @Operation(summary = "지도 기반 주차장 검색", description = "지도에서 주차장을 검색하는 API입니다.")
    @PostMapping("/map-search")
    fun searchParkingLots(@RequestBody request: ParkingLotSearchRequest):
            ResponseEntity<ResponseDto<PageResponse<ParkingLotListResponse>>> {
        val results = parkingLotSearchService.search(request)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, results))
    }
    @Operation(summary = "텍스트 기반 주차장 검색", description = "검색어로 주차장을 검색하는 API입니다.")
    @PostMapping("/text-search")
    fun searchByText(@RequestBody request: ParkingLotSearchRequest):
            ResponseEntity<ResponseDto<PageSearchResponse<ParkingLotListResponse>>> {
        val results = parkingLotSearchService.searchByText(request)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, results))
    }
}