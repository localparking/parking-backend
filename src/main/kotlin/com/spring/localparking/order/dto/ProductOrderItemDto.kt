package com.spring.localparking.order.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class ProductOrderItemDto (
    @field:NotNull
    @Schema(description = "상품 ID", example = "1")
    val productId: Long,

    @field:Positive(message = "상품 수량은 1개 이상이어야 합니다.")
    @Schema(description = "상품 수량", example = "2")
    val quantity: Int
)