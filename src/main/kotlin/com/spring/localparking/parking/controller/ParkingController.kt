package com.spring.localparking.parking.controller

import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.dto.PageResponse
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.parking.service.ParkingLotService
import com.spring.localparking.parking.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "주차장 컨트롤러", description = "주차장 관련 API입니다.")
@RestController
@RequestMapping("/parking")
class ParkingLotController(
    private val parkingLotService: ParkingLotService
) {
    @Operation(summary = "지도 기반 주차장 검색", description = "지도에서 주차장을 검색하는 API입니다.")
    @PostMapping("/map/search")
    fun searchParkingLots(@AuthenticationPrincipal principal: CustomPrincipal, @RequestBody request: ParkingLotSearchRequest):
            ResponseEntity<ResponseDto<PageResponse<ParkingLotListResponse>>> {
        val results = parkingLotService.search(request)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, results))
    }
    @Operation(summary = "주차장 상세 정보 조회", description = "주차장 상세 정보를 조회하는 API입니다.")
    @GetMapping("/detail/{parkingCode}")
    fun getParkingLotDetail(@AuthenticationPrincipal principal: CustomPrincipal, @PathVariable parkingCode: String):
            ResponseEntity<ResponseDto<ParkingLotDetailResponse>> {
        val detail = parkingLotService.getDetail(parkingCode)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, detail))
    }
}