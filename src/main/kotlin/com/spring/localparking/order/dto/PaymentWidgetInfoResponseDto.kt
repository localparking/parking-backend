package com.spring.localparking.order.dto

import com.spring.localparking.order.domain.Order

data class PaymentWidgetInfoResponseDto(
    val orderId: String,
    val orderName: String,
    val amount: Int,
    val customerName: String?,
    val customerEmail: String?
){
    companion object {
        fun from(order: Order): PaymentWidgetInfoResponseDto {
            return PaymentWidgetInfoResponseDto(
                orderId = order.id.toString(),
                orderName = "${order.orderItems.first().product.name} 등 ${order.orderItems.size}건",
                amount = order.totalPrice,
                customerName = order.user.userProfile?.name,
                customerEmail = order.user.email
            )
        }
    }
}
