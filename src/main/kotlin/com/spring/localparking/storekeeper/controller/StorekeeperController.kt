package com.spring.localparking.storekeeper.controller

import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.storekeeper.dto.BenefitRequestDto
import com.spring.localparking.storekeeper.dto.MyStoreInfo
import com.spring.localparking.storekeeper.dto.ProductRequestDto
import com.spring.localparking.storekeeper.service.StorekeeperService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "가게주인 컨트롤러", description = "가게주인 관련 API입니다.")
@RestController
@RequestMapping("/storekeeper")
class StorekeeperController(
    private val storekeeperService:StorekeeperService
) {
    @Operation(summary = "가게 소유권 신청", description = "로그인한 사용자가 특정 가게의 소유권을 신청합니다.")
    @PostMapping("/request-ownership/{storeId}")
    fun requestOwnership(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable storeId: Long,
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.requestOwnership(storeId, userId)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
    @Operation(summary = "내 가게 목록 조회", description = "로그인한 가게 주인이 소유한 가게들의 목록을 조회합니다.")
    @GetMapping("/my-stores")
    fun getMyStores(
        @AuthenticationPrincipal principal: CustomPrincipal
    ): ResponseEntity<ResponseDto<List<MyStoreInfo>>> {
        val userId = principal.id ?: throw UnauthorizedException()
        val myStores = storekeeperService.getMyStores(userId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, myStores))
    }
    @Operation(summary = "가게 주차 할인 혜택 추가", description = "특정 가게에 구매 금액별 주차 할인 혜택을 추가합니다.")
    @PostMapping("/{storeId}/benefits")
    fun addParkingBenefit(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable storeId: Long,
        @Valid @RequestBody request: BenefitRequestDto
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.addParkingBenefit(storeId, userId, request)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
    @Operation(summary = "가게 주차 할인 혜택 수정", description = "가게의 주차 할인 혜택을 수정합니다.")
    @PutMapping("/benefits/{benefitId}")
    fun updateParkingBenefit(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable benefitId: Long,
        @Valid @RequestBody request: BenefitRequestDto
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.updateParkingBenefit(benefitId, userId, request)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
    @Operation(summary = "가게 주차 할인 혜택 삭제", description = "가게의 주차 할인 혜택을 삭제합니다.")
    @DeleteMapping("/benefits/{benefitId}")
    fun deleteParkingBenefit(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable benefitId: Long
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.deleteParkingBenefit(benefitId, userId)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }

    @Operation(summary = "가게 상품 추가", description = "특정 가게에 새로운 상품을 추가합니다.")
    @PostMapping("/{storeId}/products")
    fun addProduct(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable storeId: Long,
        @Valid @RequestBody request: ProductRequestDto
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.addProduct(storeId, userId, request)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }

    @Operation(summary = "가게 상품 수정", description = "특정 상품의 정보를 수정합니다.")
    @PutMapping("/products/{productId}")
    fun updateProduct(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable productId: Long,
        @Valid @RequestBody request: ProductRequestDto
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.updateProduct(productId, userId, request)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }

    @Operation(summary = "가게 상품 삭제", description = "특정 상품을 삭제합니다.")
    @DeleteMapping("/products/{productId}")
    fun deleteProduct(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable productId: Long
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.deleteProduct(productId, userId)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
}