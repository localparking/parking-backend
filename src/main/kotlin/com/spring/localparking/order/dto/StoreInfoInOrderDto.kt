package com.spring.localparking.order.dto

import com.spring.localparking.category.dto.CategoryDto

data class StoreInfoInOrderDto(
    val storeId: Long,
    val categoryDto: CategoryDto,
    val storeName: String,
    val storeAddress: String?,
    val storeTodayClosingTime: String?
)