package com.spring.localparking.search.dto

data class StoreSimpleResponse(
    val storeId: Long,
    val name: String,
    val address: String?
)