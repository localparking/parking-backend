package com.spring.localparking.storekeeper.dto

import com.spring.localparking.storekeeper.domain.StoreParkingBenefit

data class ParkingBenefitDto(
    val benefitId: Long,
    val purchaseAmount: Int,
    val discountMin: Int
) {
    companion object {
        fun from(entity: StoreParkingBenefit) = ParkingBenefitDto(
            benefitId = entity.id,
            purchaseAmount = entity.purchaseAmount,
            discountMin = entity.discountMin
        )
    }
}