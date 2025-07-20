package com.spring.localparking.store.dto

import com.spring.localparking.store.domain.StoreDocument

data class StoreListResponse(
    val storeId: Long,
    val name: String,
    val primaryCategoryName: String?,
    val categoryNames: List<String>,
    val address: String?,
    val lat: Double,
    val lon: Double,
    val isOpen: Boolean? = null,
    val isCoalition: Boolean = false
) {
    companion object {
        fun of(doc: StoreDocument, isOpen: Boolean?): StoreListResponse {
            return StoreListResponse(
                storeId = doc.id,
                name = doc.name,
                primaryCategoryName = doc.primaryCategoryName,
                categoryNames = doc.categoryNames,
                address = doc.fullDoroAddress ?: doc.fullJibeonAddress,
                lat = doc.location.lat,
                lon = doc.location.lon,
                isOpen = isOpen,
                isCoalition = doc.isCoalition
            )
        }
    }
}