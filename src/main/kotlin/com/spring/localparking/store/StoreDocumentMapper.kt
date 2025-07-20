package com.spring.localparking.store

import com.spring.localparking.operatingHour.domain.DocumentOperatingHour
import com.spring.localparking.store.domain.Store
import com.spring.localparking.store.domain.StoreDocument
import org.springframework.data.elasticsearch.core.geo.GeoPoint

object StoreDocumentMapper {
    fun toDocument(store: Store): StoreDocument? {
        val loc = store.location ?: return null
        val lat = loc.lat ?: return null
        val lon = loc.lon ?: return null
        val catIds = store.categories.mapNotNull { it.category?.id }
        val catNames = store.categories.mapNotNull { it.category?.name }
        val primaryId = catIds.firstOrNull()
        val primaryName = catNames.firstOrNull()

        val docHours = store.operatingHour?.timeSlots?.map {
            DocumentOperatingHour(
                dayOfWeek = it.dayOfWeek,
                beginTime = it.beginTime.hour * 100 + it.beginTime.minute,
                endTime = it.endTime.hour * 100 + it.endTime.minute,
                isOvernight = it.endTime.isBefore(it.beginTime)
            )
        } ?: emptyList()

        return StoreDocument(
            id = store.id,
            name = store.name,
            categoryIds = catIds,
            categoryNames = catNames,
            primaryCategoryId = primaryId,
            primaryCategoryName = primaryName,
            fullDoroAddress = loc.doroAddress?.fullAddress,
            fullJibeonAddress = loc.jibeonAddress?.fullAddress,
            sido = loc.doroAddress?.sido,
            sigungu = loc.doroAddress?.sigungu,
            isCoalition = store.isCoalition,
            maxFreeMin = store.maxFreeMin,
            location = GeoPoint(lat, lon),
            operatingHours = docHours
        )
    }
}
