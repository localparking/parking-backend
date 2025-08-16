package com.spring.localparking.order.controller

import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.order.dto.OrderRequestDto
import com.spring.localparking.order.dto.OrderResponseDto
import com.spring.localparking.order.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "주문 컨트롤러", description = "주문 관련 API입니다.")
@RestController
@RequestMapping("/order")
class OrderController (
    private val orderService: OrderService
){
    @Operation(summary = "상품 주문하기", description = "장바구니에 담긴 상품들을 주문합니다.")
    @PostMapping("/{storeId}")
    fun orderProducts(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable storeId: Long,
        @Valid @RequestBody req : OrderRequestDto
    ): ResponseEntity<ResponseDto<OrderResponseDto>> {
        val userId = principal.id ?: throw UnauthorizedException()
        val result = orderService.placeOrder(userId, storeId, req)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, result))
    }
}