package com.spring.localparking.storekeeper.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "주차 혜택 추가/수정 요청 DTO")
data class ParkingBenefitRequestDto(
    @field:Positive(message = "구매 금액은 0보다 커야 합니다.")
    @field:NotNull
    @Schema(description = "혜택 적용을 위한 최소 구매 금액", example = "30000")
    val purchaseAmount: Int?,

    @field:Positive(message = "할인 시간은 0보다 커야 합니다.")
    @Schema(description = "제공되는 무료 주차 시간(분)", example = "60")
    @field:NotNull
    val discountMin: Int?
)