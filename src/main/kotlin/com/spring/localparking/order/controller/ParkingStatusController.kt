package com.spring.localparking.order.controller

import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.order.dto.ParkingStatusResponseDto
import com.spring.localparking.order.service.ParkingStatusService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "주차 현황 컨트롤러", description = "실시간 주차 현황 관련 API입니다.")
@RestController
@RequestMapping("/parking")
class ParkingStatusController(
    private val parkingStatusService: ParkingStatusService
) {
    @Operation(summary = "현재 주차 현황 조회", description = "오늘 주차한 내역 중 출차되지 않은 가장 최근 건을 조회합니다.")
    @GetMapping("/status")
    fun getParkingStatus(
        @AuthenticationPrincipal principal: CustomPrincipal
    ): ResponseEntity<ResponseDto<ParkingStatusResponseDto>> {
        val userId = principal.id ?: throw UnauthorizedException()
        val response = parkingStatusService.getParkingStatus(userId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, response))
    }

    @Operation(summary = "입차 시간 업데이트", description = "주문 내역의 방문 시간을 현재 시간으로 업데이트합니다.")
    @PostMapping("/{orderId}/arrival")
    fun updateVisitTimeToNow(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable orderId: String
    ): ResponseEntity<ResponseDto<ParkingStatusResponseDto>> {
        val userId = principal.id ?: throw UnauthorizedException()
        val result = parkingStatusService.updateVisitTimeToNow(userId, orderId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, result))
    }

    @Operation(summary = "출차 처리하기", description = "주문 내역을 출차 처리합니다.")
    @PostMapping("/{orderId}/departure")
    fun processDeparture(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable orderId: String
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        parkingStatusService.processDeparture(userId, orderId)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
}