package com.spring.localparking.order.dto

data class OrderItemInOrderDto(
    val productName: String,
    val quantity: Int,
    val price: Int
)