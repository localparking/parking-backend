package com.spring.localparking.storekeeper.dto

import jakarta.validation.constraints.Min

data class BenefitRequestDto(
    @field:Min(value = 0, message = "구매 금액은 0 이상 이어야 합니다.")
    val purchaseAmount: Int,

    @field:Min(value = 30, message = "할인 시간은 30분 이상이어야 합니다.")
    val discountMin: Int
)