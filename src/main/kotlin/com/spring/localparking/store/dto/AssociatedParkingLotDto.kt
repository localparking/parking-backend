package com.spring.localparking.store.dto

import com.spring.localparking.parking.dto.AssociatedStoreDto

data class AssociatedParkingLotDto (
    val parkingCode: String,
    val name: String,
    val isOpen: Boolean? = null,
    val hourlyFee: Int? = null,
    val capacity: Int? = null,
    val curCapacity: Int? = null,
    val otherStores: List<AssociatedStoreDto>
)
