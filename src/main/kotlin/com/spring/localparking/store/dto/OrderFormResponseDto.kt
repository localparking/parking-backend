package com.spring.localparking.store.dto

import com.spring.localparking.storekeeper.dto.ParkingBenefitDto
import com.spring.localparking.storekeeper.dto.ProductResponseDto

data class OrderFormResponseDto (
    val storeId: Long,
    val storeName: String,
    val benefits: List<ParkingBenefitDto>,
    val products: List<ProductResponseDto>
)