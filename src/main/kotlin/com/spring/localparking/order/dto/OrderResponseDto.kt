package com.spring.localparking.order.dto

import com.spring.localparking.order.domain.Order
import com.spring.localparking.parking.domain.openStatus
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
data class OrderResponseDto(
    val orderId: UUID,
    val createdAt: LocalDateTime,

    val visitorName: String,
    val visitorTel: String,
    val visitorRegionName: String,
    val visitorVehicleNumber: String,
    val visitTime: LocalDateTime,

    val storeInfo: StoreInfoInOrderDto,
    val parkingLotInfo: ParkingLotInfoInOrderDto?,

    val orderItems: List<OrderItemInOrderDto>,

    val totalPrice: Int,
    val totalDiscount: Int,
    val parkingDiscountMin: Int,
    val parkingFeeDiscount: Int
) {
    companion object {
        fun from(order: Order): OrderResponseDto {
            val now = LocalDateTime.now()
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val primaryParkingLot = order.store.storeParkingLots.firstOrNull()?.parkingLot

            val storeInfo = StoreInfoInOrderDto(
                storeId = order.store.id,
                storeName = order.store.name,
                storeAddress = order.store.location.doroAddress?.fullAddress ?: order.store.location.jibeonAddress?.fullAddress,
                storeTodayClosingTime = order.store.operatingHour?.openStatus(now)?.second?.format(timeFormatter)
            )

            val parkingLotInfo = primaryParkingLot?.let {
                ParkingLotInfoInOrderDto(
                    parkingLotId = it.parkingCode,
                    parkingLotName = it.name,
                    parkingLotAddress = it.address,
                    parkingLotTodayClosingTime = it.operatingHour?.openStatus(now)?.second?.let { time ->
                        if (time == LocalTime.MAX || time == LocalTime.of(23, 59)) "24:00" else time.format(timeFormatter)
                    }
                )
            }

            val orderItemsInfo = order.orderItems.map {
                OrderItemInOrderDto(
                    productName = it.product.name,
                    quantity = it.quantity,
                    price = it.orderPrice
                )
            }

            return OrderResponseDto(
                orderId = order.id!!,
                createdAt = order.createdAt,
                visitorName = order.visitorName,
                visitorTel = order.visitorTel,
                visitorRegionName = order.visitorRegionName,
                visitorVehicleNumber = order.visitorVehicleNumber,
                visitTime = order.visitTime,
                storeInfo = storeInfo,
                parkingLotInfo = parkingLotInfo,
                orderItems = orderItemsInfo,
                totalPrice = order.totalPrice,
                totalDiscount = order.totalDiscount,
                parkingDiscountMin = order.parkingDiscountMin,
                parkingFeeDiscount = order.parkingFeeDiscount
            )
        }
    }
}