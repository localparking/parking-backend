package com.spring.localparking.store.dto

data class AssociatedParkingLotDto (
    val parkingLotId: Long,
    val name: String,
    val hourlyFee: Int? = null,
    val capacity: Int? = null,
    val curCapacity: Int? = null,
)
