package com.spring.localparking.search.dto

import com.spring.localparking.category.dto.CategoryDto
import com.spring.localparking.global.dto.StoreType
import com.spring.localparking.operatingHour.domain.DocumentOperatingHour
import com.spring.localparking.search.domain.StoreDocument
import java.time.LocalDateTime

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
        private fun calculateCurrentIsOpen(operatingHours: List<DocumentOperatingHour>): Boolean? {
            if (operatingHours.isEmpty()) return null
            val now = LocalDateTime.now()
            val today = now.dayOfWeek
            val yesterday = today.minus(1)
            val currentTimeInt = now.hour * 100 + now.minute
            return operatingHours.any { slot ->
                val begin = slot.beginTime
                val end = slot.endTime
                if (begin == null || end == null) {
                    false
                } else if (slot.dayOfWeek == today) {
                    (!slot.isOvernight && currentTimeInt >= begin && currentTimeInt < end) || (slot.isOvernight && currentTimeInt >= begin)
                } else if (slot.dayOfWeek == yesterday) {
                    slot.isOvernight && currentTimeInt < end
                } else {
                    false
                }
            }
        }
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
                isOpen = calculateCurrentIsOpen(doc.operatingHours),
                purchaseAmount = doc.purchaseAmount,
                discountMin = doc.discountMin
            )
        }
    }
}