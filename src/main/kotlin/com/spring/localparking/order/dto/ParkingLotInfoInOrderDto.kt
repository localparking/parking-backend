package com.spring.localparking.order.dto

data class ParkingLotInfoInOrderDto(
    val parkingLotId: String,
    val parkingLotName: String,
    val parkingLotAddress: String?,
    val parkingLotTodayClosingTime: String?
)