package com.spring.localparking.global.util

import com.spring.localparking.parking.domain.FeePolicy

object FeeCalculationUtil {
    fun calculateHourlyFee(feePolicy: FeePolicy?): Int? {
        val baseFee = feePolicy?.baseFee
        val baseTime = feePolicy?.baseTimeMin

        if (baseFee == null || baseTime == null || baseTime <= 0) return null

        if (baseTime >= 60) {
            return (baseFee * (60.0 / baseTime)).toInt()
        }

        val addFee = feePolicy.additionalFee?: return baseFee
        val addTime = feePolicy.additionalTimeMin?: return baseTime

        val remaining = 60 - baseTime
        val chunks = (remaining + addTime - 1) / addTime
        return baseFee + chunks * addFee
    }
}