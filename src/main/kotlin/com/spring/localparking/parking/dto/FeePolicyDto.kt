package com.spring.localparking.parking.dto

import com.spring.localparking.parking.domain.FeePolicy

data class FeePolicyDto(
    val baseFee: Int?,
    val baseTimeMin: Int?,
    val additionalFee: Int?,
    val additionalTimeMin: Int?,
    val dayPassFee: Int? = null,
    val monthlyPassFee: Int? = null
) {
    companion object {
        fun from(feePolicy: FeePolicy?): FeePolicyDto {
            return FeePolicyDto(
                baseFee = feePolicy?.baseFee,
                baseTimeMin = feePolicy?.baseTimeMin,
                additionalFee = feePolicy?.additionalFee,
                additionalTimeMin = feePolicy?.additionalTimeMin,
                dayPassFee = feePolicy?.dayPassFee,
                monthlyPassFee = feePolicy?.monthlyPassFee
            )
        }
    }
}