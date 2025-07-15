package com.spring.localparking.parking.dto

import com.spring.localparking.parking.domain.ParkingLotDocument

data class ParkingLotListResponse(
    val parkingCode: String,
    val name: String,
    val address: String?,
    val capacity: Int?,
    val isRealtime: Boolean?,
    val curCapacity: Int? = null,

    val baseFee: Int?,
    val baseTimeMin: Int?,
    val lat: Double,
    val lon: Double,
    val isOpen: Boolean? = null

) {
    companion object {
        fun of(doc: ParkingLotDocument, curCapacity: Int?, isOpen: Boolean? ): ParkingLotListResponse {
            return ParkingLotListResponse(
                parkingCode = doc.parkingCode,
                name = doc.name,
                address = doc.address,
                capacity = doc.capacity,
                isRealtime = doc.isRealtime,
                baseFee = doc.baseFee,
                baseTimeMin = doc.baseTimeMin,
                lat = doc.location.lat,
                lon = doc.location.lon,
                isOpen = isOpen,
                curCapacity = curCapacity
            )
        }
    }
}