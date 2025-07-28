package com.spring.localparking.store

import com.spring.localparking.global.dto.StoreType
import com.spring.localparking.operatingHour.domain.DocumentOperatingHour
import com.spring.localparking.parking.domain.is24Hours
import com.spring.localparking.parking.domain.isOpened
import com.spring.localparking.store.domain.Store
import com.spring.localparking.store.domain.StoreDocument
import org.springframework.data.elasticsearch.core.geo.GeoPoint
import java.time.LocalDateTime

object StoreDocumentMapper {
    fun toDocument(store: Store, now: LocalDateTime = LocalDateTime.now()): StoreDocument? {
        val loc = store.location
        val lat = loc.lat
        val lon = loc.lon

        val categories = store.categories
        val categoryIds = categories
            .map { it.category.id }
            .distinct()
        val categoryNames = categories
            .map { it.category.name }
            .filter { it.isNotBlank() }
            .distinct()

        val categoryParentIds = categories.map { it.category.parent?.id ?: -1L }


        val op = store.operatingHour
        val isOpen = op?.isOpened(now)
        val is24 = op?.is24Hours(now.dayOfWeek) ?: false

        val docHours = op?.timeSlots
            ?.filter { slot -> slot.beginTime != slot.endTime }
            ?.map { slot ->
                DocumentOperatingHour(
                    dayOfWeek = slot.dayOfWeek,
                    beginTime = slot.beginTime!!.hour * 100 + slot.beginTime!!.minute,
                    endTime = slot.endTime!!.hour * 100 + slot.endTime!!.minute,
                    isOvernight = slot.isOvernight()
                )
            }.orEmpty()

        val maxFreeMinutes = store.storeParkingLots
            .mapNotNull { it.parkingLot.feePolicy?.baseTimeMin }
            .maxOrNull()

        val representativeBenefit = store.parkingBenefits.minByOrNull { it.purchaseAmount }
        return StoreDocument(
            id = store.id,
            name = store.name,
            storeType = store.storeType ?: StoreType.GENERAL,
            categoryIds = categoryIds,
            categoryNames = categoryNames,
            categoryParentIds = categoryParentIds,
            fullDoroAddress = loc.doroAddress?.fullAddress,
            fullJibeonAddress = loc.jibeonAddress?.fullAddress,
            sido = loc.doroAddress?.sido,
            sigungu = loc.doroAddress?.sigungu,
            location = GeoPoint(lat, lon),
            freeMinutes = maxFreeMinutes,
            purchaseAmount = representativeBenefit?.purchaseAmount,
            discountMin = representativeBenefit?.discountMin,
            isOpen = isOpen,
            is24Hours = is24,
            operatingHours = docHours
        )
    }
}
