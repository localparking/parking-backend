package com.spring.localparking.parking.dto

import com.spring.localparking.operatingHour.domain.is24Hours
import com.spring.localparking.operatingHour.domain.openStatus
import com.spring.localparking.operatingHour.dto.GroupedOperatingHoursDto
import com.spring.localparking.operatingHour.dto.buildGroupedWeek
import com.spring.localparking.parking.domain.ParkingLot
import java.time.LocalDateTime
import java.time.LocalTime
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
    val operatingHours: List<GroupedOperatingHoursDto>,
    val associatedStores: List<AssociatedStoreDto>
) {
    companion object {
        private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm")
        fun from(
            entity: ParkingLot,
            congestion: String?,
            curCapacity: Int?,
            associatedStores: List<AssociatedStoreDto>,
            now: LocalDateTime = LocalDateTime.now()
        ): ParkingLotDetailResponse {

            val op = entity.operatingHour
            val (open, rawClosing) = op?.openStatus(now) ?: (null to null)
            val is24 = op?.is24Hours(now.dayOfWeek) == true
            val closingStr = when {
                open == true && is24 -> "24:00"
                rawClosing != null && rawClosing == LocalTime.MAX -> "24:00"
                else -> rawClosing?.format(TIME_FMT)
            }
            val hours = buildGroupedWeek(op)

            return ParkingLotDetailResponse(
                parkingCode = entity.parkingCode,
                name = entity.name,
                address = entity.address,
                isOpen = open,
                capacity = entity.capacity,
                curCapacity = curCapacity,
                congestion = congestion,
                todayClosingTime = closingStr,
                isRealtime = entity.isRealtime,
                isFree = entity.isFree,
                tel = entity.tel,
                lat = entity.lat,
                lon = entity.lon,
                feePolicy = FeePolicyDto.from(entity.feePolicy),
                operatingHours = hours,
                associatedStores = associatedStores
            )
        }
    }
}
