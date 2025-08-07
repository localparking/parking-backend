package com.spring.localparking.storekeeper.dto

import com.spring.localparking.auth.dto.storekeeper.StoreAddressDto
import com.spring.localparking.category.dto.CategoryDto
import com.spring.localparking.operatingHour.DailyOperatingDto
import com.spring.localparking.operatingHour.OperatingHourPresenter
import com.spring.localparking.store.domain.Store


data class MyStoreResponse(
    val storeName: String,
    val businessNumber: String?,
    val tel: String?,
    val storeAddress: StoreAddressDto,
    val categories: List<CategoryDto>,
    val operatingHours: List<DailyOperatingDto>?
) {
    companion object {
        fun from(store: Store): MyStoreResponse {
            val loc = store.location
            return MyStoreResponse(
                storeName = store.name,
                businessNumber = store.businessNumber,
                tel = store.tel,
                storeAddress = StoreAddressDto(
                    sido = loc.doroAddress?.sido ?: "",
                    sigungu = loc.doroAddress?.sigungu ?: "",
                    doroName = loc.doroAddress?.doroName ?: "",
                    buildingNo = loc.doroAddress?.buildingNo ?: "",
                    lat = loc.lat,
                    lon = loc.lon
                ),
                categories = store.categories.map {
                    CategoryDto(it.category.id, it.category.name, it.category.parent?.id)
                },
                operatingHours = OperatingHourPresenter.build(store.operatingHour)
            )
        }
    }
}