package com.spring.localparking.storekeeper.dto

class MyStoreInfo(
    val storeId: Long,
    val storeName: String,
    val storeAddress: String,
    val storePhone: String?,
    val ownerName: String? = null,
    val parkingBenefits: List<ParkingBenefitDto> = emptyList()
)