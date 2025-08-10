package com.spring.localparking.storekeeper.controller

import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.storekeeper.dto.ParkingBenefitDto
import com.spring.localparking.storekeeper.dto.ParkingBenefitRequestDto
import com.spring.localparking.storekeeper.service.StorekeeperService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "점주 주차혜택 관리 컨트롤러", description = "점주가 자신의 가게에 주차혜택을 관리하는 API입니다.")
@RestController
@RequestMapping("/storekeeper")
class StorekeeperBenefitController(
    private val storekeeperService: StorekeeperService
) {
    @Operation(summary = "주차 혜택 목록 조회", description = "내 가게에 설정된 모든 주차 혜택(구매 금액별 할인)을 조회합니다.")
    @GetMapping("/benefits")
    fun getParkingbenefits(
        @AuthenticationPrincipal principal: CustomPrincipal
    ): ResponseEntity<ResponseDto<List<ParkingBenefitDto>>> {
        val userId = principal.id ?: throw UnauthorizedException()
        val benefits = storekeeperService.getParkingBenefits(userId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, benefits))
    }

    @Operation(summary = "주차 혜택 추가", description = "내 가게에 새로운 주차 혜택(구매 금액별 할인)을 추가합니다.")
    @PostMapping("/benefits")
    fun addParkingBenefit(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @Valid @RequestBody request: ParkingBenefitRequestDto
    ): ResponseEntity<ResponseDto<ParkingBenefitDto>> {
        val userId = principal.id ?: throw UnauthorizedException()
        val benefitResponse = storekeeperService.addParkingBenefit(userId, request)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, benefitResponse))
    }

    @Operation(summary = "주차 혜택 수정", description = "기존 주차 혜택의 내용을 수정합니다.")
    @PutMapping("/benefits/{benefitId}")
    fun updateParkingBenefit(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable benefitId: Long,
        @Valid @RequestBody request: ParkingBenefitRequestDto
    ): ResponseEntity<ResponseDto<ParkingBenefitDto>> {
        val userId = principal.id ?: throw UnauthorizedException()
        val updatedBenefit = storekeeperService.updateParkingBenefit(userId, benefitId, request)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, updatedBenefit))
    }

    @Operation(summary = "주차 혜택 삭제", description = "가게의 특정 주차 혜택을 삭제합니다.")
    @DeleteMapping("/benefits/{benefitId}")
    fun deleteParkingBenefit(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable benefitId: Long
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.deleteParkingBenefit(userId, benefitId)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
}