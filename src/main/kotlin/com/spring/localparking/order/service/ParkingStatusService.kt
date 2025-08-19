package com.spring.localparking.order.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.global.util.FeeCalculationUtil
import com.spring.localparking.order.dto.ParkingStatusResponseDto
import com.spring.localparking.order.repository.OrderRepository
import com.spring.localparking.parking.domain.openStatus
import com.spring.localparking.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class ParkingStatusService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getParkingStatus(userId: Long): ParkingStatusResponseDto {
        val user = userRepository.findById(userId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        val order = orderRepository.findTopByUserAndIsDepartedFalseOrderByCreatedAtDesc(user)
            ?: throw CustomException(ErrorCode.ORDER_NOT_FOUND)

        val primaryParkingLot = order.store.storeParkingLots.firstOrNull()?.parkingLot
            ?: throw CustomException(ErrorCode.PARKING_LOT_NOT_FOUND)

        val visitTime = order.visitTime
        val freeParkingUntil = visitTime.plusMinutes(order.parkingDiscountMin.toLong())

        var extraCharge = 0
        if (LocalDateTime.now().isAfter(freeParkingUntil)) {
            val overtimeMinutes = Duration.between(freeParkingUntil, LocalDateTime.now()).toMinutes()
            extraCharge = FeeCalculationUtil.calculateParkingFeeForDuration(overtimeMinutes, primaryParkingLot.feePolicy)
        }
        fun formatClosingTime(time: LocalTime?): String? {
            return when {
                time != null && (time == LocalTime.MAX || (time.hour == 23 && time.minute == 59)) -> "24:00"
                else -> time?.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
        }
        return ParkingStatusResponseDto(
            orderId = order.id.toString(),
            visitTime = visitTime,
            freeParkingUntil = freeParkingUntil,
            extraCharge = extraCharge,
            parkingFeeDiscount = order.parkingFeeDiscount,
            parkingLotName = primaryParkingLot.name,
            parkingLotTodayClosingTime = formatClosingTime(primaryParkingLot.operatingHour?.openStatus()?.second),
            parkingLotAddress = primaryParkingLot.address,
            baseTimeMin = primaryParkingLot.feePolicy?.baseTimeMin,
            baseFee = primaryParkingLot.feePolicy?.baseFee,
            additionalTimeMin = primaryParkingLot.feePolicy?.additionalTimeMin,
            additionalFee = primaryParkingLot.feePolicy?.additionalFee
        )
    }

    @Transactional
    fun updateVisitTimeToNow(userId: Long, orderId: UUID) : ParkingStatusResponseDto{
        val order = orderRepository.findById(orderId).orElseThrow { CustomException(ErrorCode.ORDER_NOT_FOUND) }
        if (order.user.id != userId) {
            throw CustomException(ErrorCode.ACCESS_DENIED)
        }
        order.visitTime = LocalDateTime.now()
        orderRepository.save(order)
        return getParkingStatus(userId)
    }

    @Transactional
    fun processDeparture(userId: Long, orderId: UUID) {
        val order = orderRepository.findById(orderId).orElseThrow { CustomException(ErrorCode.ORDER_NOT_FOUND) }
        if (order.user.id != userId) {
            throw CustomException(ErrorCode.ACCESS_DENIED)
        }
        order.isDeparted = true
        orderRepository.save(order)
    }
}