package com.spring.localparking.store.dto

import com.spring.localparking.global.dto.StoreType
import com.spring.localparking.operatingHour.DailyOperatingDto
import com.spring.localparking.operatingHour.OperatingHourPresenter
import com.spring.localparking.operatingHour.domain.OperatingHour
import com.spring.localparking.parking.domain.openStatus
import com.spring.localparking.store.domain.Store
import java.time.format.DateTimeFormatter


data class StoreDetailResponse (
    val storeId: Long,
    val name: String,
    val storeType: StoreType? = StoreType.GENERAL,
    val categoryNames: List<String>?,
    val address: String?,
    val isOpen: Boolean? = null,
    val todayClosingTime: String?,
    val tel: String? = null,
    val lat: Double,
    val lon: Double,
    val maxFreeMin: Int? = null,
    val operatingTable: List<DailyOperatingDto>,
    val associatedParkingLots: List<AssociatedParkingLotDto>
){
    companion object {
        private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm")
        fun from(entity: Store, associatedParkingLots: List<AssociatedParkingLotDto>): StoreDetailResponse {
            val op: OperatingHour? = entity.operatingHour
            val (open, closingLocalTime) = op?.openStatus() ?: (null to null)
            val closingStr = closingLocalTime?.format(TIME_FMT)

            val categoryNames = entity.categories.mapNotNull { it.category?.name }
            val loc = entity.location


            return StoreDetailResponse(
                storeId = entity.id,
                name = entity.name,
                storeType = entity.storeType,
                categoryNames = categoryNames,
                address = loc.doroAddress?.fullAddress
                    ?: loc.jibeonAddress?.fullAddress,
                isOpen = open,
                todayClosingTime = closingStr,
                tel = entity.tel,
                lat = loc.lat,
                lon = loc.lon,
                maxFreeMin = entity.maxFreeMin,
                operatingTable = OperatingHourPresenter.build(op),
                associatedParkingLots = associatedParkingLots
            )
        }
    }
}
