package com.spring.localparking.store.dto

import com.spring.localparking.store.domain.StoreDocument

data class StoreListResponse(
    val storeId: Long,
    val name: String,
    val storeType: String,
    val categoryNames: List<String>,
    val lat: Double,
    val lon: Double,
    val isOpen: Boolean? = null,
) {
    companion object {
        fun of(doc: StoreDocument): StoreListResponse {
            return StoreListResponse(
                storeId = doc.id,
                name = doc.name,
                storeType = doc.storeType.name,
                categoryNames = doc.categoryNames,
                lat = doc.location.lat,
                lon = doc.location.lon,
                isOpen = doc.isOpen,
            )
        }
    }
}