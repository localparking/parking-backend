package com.spring.localparking.order.controller

import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.order.dto.OrderRequestDto
import com.spring.localparking.order.dto.OrderResponseDto
import com.spring.localparking.order.dto.PaymentWidgetInfoResponseDto
import com.spring.localparking.order.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

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
    ): ResponseEntity<ResponseDto<PaymentWidgetInfoResponseDto>> {
        val userId = principal.id ?: throw UnauthorizedException()
        val result = orderService.placeOrder(userId, storeId, req)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, result))
    }

    @Operation(summary = "결제 성공 후처리", description = "프론트에서 결제 성공 후 호출되어 최종 결제 승인을 진행합니다.")
    @GetMapping("/success")
    fun handlePaymentSuccess(
        @RequestParam paymentKey: String,
        @RequestParam orderId: String,
        @RequestParam amount: Int
    ): ResponseEntity<ResponseDto<Unit>> {
        orderService.confirmPayment(paymentKey, orderId, amount)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }

    @Operation(summary = "결제 완료 건 상세 조회", description = "결제가 완료된 특정 주문 건의 상세 내역을 조회합니다.")
    @GetMapping("/{orderId}")
    fun getPaidOrderDetail(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable orderId: UUID
    ): ResponseEntity<ResponseDto<OrderResponseDto>> {
        val userId = principal.id ?: throw UnauthorizedException()
        val orderDetail = orderService.getPaidOrderDetail(userId, orderId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, orderDetail))
    }
}