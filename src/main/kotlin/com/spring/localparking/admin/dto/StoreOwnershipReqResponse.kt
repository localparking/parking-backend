package com.spring.localparking.admin.dto



data class StoreOwnershipReqResponse (
    val requestId: Long,
    val storeId: Long,
    val userId: Long,
    val status: String,
    val storeName: String,
    val userName: String
)