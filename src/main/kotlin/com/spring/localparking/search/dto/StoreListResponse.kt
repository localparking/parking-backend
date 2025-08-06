package com.spring.localparking.search.dto

import com.spring.localparking.category.dto.CategoryDto
import com.spring.localparking.global.dto.StoreType
import com.spring.localparking.search.domain.StoreDocument

data class StoreListResponse(
    val storeId: Long,
    val name: String,
    val address: String,
    val storeType: String? = StoreType.GENERAL.name,
    val categories: List<CategoryDto>?,
    val lat: Double,
    val lon: Double,
    val isOpen: Boolean? = null,
    val purchaseAmount: Int?,
    val discountMin: Int?
) {
    companion object {
        fun of(doc: StoreDocument): StoreListResponse {

            val categoryInfoList = doc.categoryIds.indices.map { index ->
                CategoryDto(
                    categoryId = doc.categoryIds[index],
                    categoryName = doc.categoryNames.getOrElse(index) { "" },
                    parentId = doc.categoryParentIds.getOrNull(index)?.let { if (it == -1L) null else it }
                )
            }
            return StoreListResponse(
                storeId = doc.id,
                name = doc.name,
                address = doc.fullDoroAddress ?: doc.fullJibeonAddress ?: "",
                storeType = doc.storeType?.name,
                categories = categoryInfoList,
                lat = doc.location.lat,
                lon = doc.location.lon,
                isOpen = doc.isOpen,
                purchaseAmount = doc.purchaseAmount,
                discountMin = doc.discountMin
            )
        }
    }
}