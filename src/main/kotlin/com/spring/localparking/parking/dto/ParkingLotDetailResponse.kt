package com.spring.localparking.parking.dto

import com.spring.localparking.parking.domain.ParkingLot
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ParkingLotDetailResponse(
    val parkingCode: String,
    val name: String,
    val address: String?,
    val isOpen: Boolean? = null,
    val capacity: Int?,
    val curCapacity: Int? = null,
    val congestion: String?,
    val todayClosingTime: String?,
    val isRealtime: Boolean,
    val tel: String? = null,
    val isFree: Boolean,
    val lat: Double?,
    val lon: Double?,
    val feePolicy: FeePolicyDto,
    val operatingHours: List<OperatingHoursDto>,
    val associatedStores: List<AssociatedStoreDto>
) {
    companion object {
        fun from(entity: ParkingLot, congestion: String?, curCapacity: Int?,
                 isOpen: Boolean?, associatedStores: List<AssociatedStoreDto>): ParkingLotDetailResponse {
            val operatingHour = entity.operatingHour
            val operatingHoursList = mutableListOf<OperatingHoursDto>()
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            val weekdaySlot = operatingHour?.timeSlots?.find { it.dayOfWeek == DayOfWeek.MONDAY }
            operatingHoursList.add(OperatingHoursDto.from("평일", weekdaySlot))

            val weekendSlot = operatingHour?.timeSlots?.find { it.dayOfWeek == DayOfWeek.SATURDAY }
            operatingHoursList.add(OperatingHoursDto.from("주말", weekendSlot))

            val holidaySlot = operatingHour?.timeSlots?.find { it.dayOfWeek == DayOfWeek.SUNDAY }
            operatingHoursList.add(OperatingHoursDto.from("공휴일", holidaySlot))

            val today = LocalDate.now().dayOfWeek
            val todaySlot = operatingHour?.timeSlots?.find { it.dayOfWeek == today }
            val todayClosingTime = todaySlot?.endTime?.format(timeFormatter)

            return ParkingLotDetailResponse(
                parkingCode = entity.parkingCode,
                name = entity.name,
                address = entity.address,
                isOpen = isOpen,
                capacity = entity.capacity,
                curCapacity = curCapacity,
                congestion = congestion,
                todayClosingTime = todayClosingTime,
                isRealtime = entity.isRealtime,
                isFree = entity.isFree,
                tel = entity.tel,
                lat = entity.lat,
                lon = entity.lon,
                feePolicy = FeePolicyDto.from(entity.feePolicy),
                operatingHours = operatingHoursList,
                associatedStores = associatedStores
            )
        }
    }
}
