package com.spring.localparking.storekeeper.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

@Schema(description = "가게 상품 추가 요청 DTO")
data class ProductRequestDto(
    @field:NotBlank(message = "상품 이름은 필수입니다.")
    @Schema(description = "상품 이름", example = "마라탕")
    val name: String,

    @Schema(description = "상품 이미지 URL", example = "https://example.com/image.jpg")
    val imageUrl: String,

    @Schema(description = "상품 설명", example = "맛있는 마라탕~!")
    val description: String,

    @field:PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    @field: NotNull
    @Schema(description = "상품 가격", example = "50000")
    val price: Int
)