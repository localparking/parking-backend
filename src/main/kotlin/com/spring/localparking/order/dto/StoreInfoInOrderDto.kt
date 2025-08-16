package com.spring.localparking.order.dto

data class StoreInfoInOrderDto(
    val storeId: Long,
    val storeName: String,
    val storeAddress: String?,
    val storeTodayClosingTime: String?
)