package com.spring.localparking.order.dto

import com.spring.localparking.user.dto.VisitorInfo
import jakarta.validation.Valid
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class OrderRequestDto (
    @field:Valid
    @field:NotNull
    val visitorInfo: VisitorInfo,

    @field:NotEmpty
    val orderItems: List<ProductOrderItemDto>,

    @field:NotNull
    @field:Future(message = "방문예정시간은 현재 시간 이후여야 합니다.")
    val visitTime: LocalDateTime,

    @field:Positive(message = "총 결제 금액은 0보다 커야 합니다.")
    val clientTotalPrice: Int
)