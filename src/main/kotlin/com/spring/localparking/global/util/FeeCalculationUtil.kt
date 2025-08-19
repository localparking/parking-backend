package com.spring.localparking.global.util

import com.spring.localparking.parking.domain.FeePolicy
import kotlin.math.ceil

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
    fun calculateParkingFeeForDuration(durationMin: Long, feePolicy: FeePolicy?): Int {

        if (durationMin <= 0 || feePolicy == null) return 0
        val additionalTime = feePolicy.additionalTimeMin ?: return feePolicy.baseTimeMin
        val additionalFee = feePolicy.additionalFee ?: return feePolicy.baseFee
        val chunks = ceil(durationMin.toDouble() / additionalTime.toDouble()).toInt()

        return chunks * additionalFee
    }
}