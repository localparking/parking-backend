package com.spring.localparking.search.dto

import com.spring.localparking.operatingHour.domain.DocumentOperatingHour
import com.spring.localparking.search.domain.ParkingLotDocument
import java.time.LocalDateTime

data class ParkingLotListResponse(
    val parkingCode: String,
    val name: String,
    val address: String?=null,
    val capacity: Int?,
    val isRealtime: Boolean?,
    val curCapacity: Int? = null,
    val hourlyFee: Int? = null,
    val lat: Double,
    val lon: Double,
    val isOpen: Boolean? = null

) {
    companion object {
        private fun calculateCurrentIsOpen(operatingHours: List<DocumentOperatingHour>): Boolean? {
            if (operatingHours.isEmpty()) return null

            val now = LocalDateTime.now()
            val today = now.dayOfWeek
            val yesterday = today.minus(1)
            val currentTimeInt = now.hour * 100 + now.minute

            val isOpenNow = operatingHours.any { slot ->
                val begin = slot.beginTime
                val end = slot.endTime
                if (begin == null || end == null) return@any false

                when {
                    slot.dayOfWeek == today && !slot.isOvernight ->
                        currentTimeInt >= begin && currentTimeInt < end

                    slot.dayOfWeek == today && slot.isOvernight ->
                        currentTimeInt >= begin
                    slot.dayOfWeek == yesterday && slot.isOvernight ->
                        currentTimeInt < end
                    else -> false
                }
            }
            return isOpenNow
        }
        fun of(doc: ParkingLotDocument, curCapacity: Int?): ParkingLotListResponse {
            return ParkingLotListResponse(
                parkingCode = doc.parkingCode,
                name = doc.name,
                address = doc.address,
                capacity = doc.capacity,
                isRealtime = doc.isRealtime,
                hourlyFee = doc.hourlyFee,
                lat = doc.location.lat,
                lon = doc.location.lon,
                isOpen = calculateCurrentIsOpen(doc.operatingHours),
                curCapacity = curCapacity
            )
        }
    }
}