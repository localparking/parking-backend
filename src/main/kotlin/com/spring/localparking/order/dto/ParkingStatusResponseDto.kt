package com.spring.localparking.order.dto


import java.time.LocalDateTime

data class ParkingStatusResponseDto(
    val orderId: String,
    val visitTime: LocalDateTime?,
    val freeParkingUntil: LocalDateTime?,
    val extraCharge: Int,
    val parkingFeeDiscount: Int,

    val parkingLotName: String,
    val parkingLotTodayClosingTime: String?,
    val parkingLotAddress: String?,

    val baseTimeMin: Int?,
    val baseFee: Int?,
    val additionalTimeMin: Int?,
    val additionalFee: Int?
)