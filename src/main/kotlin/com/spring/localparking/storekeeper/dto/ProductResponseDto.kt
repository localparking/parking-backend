package com.spring.localparking.storekeeper.dto

import com.spring.localparking.store.domain.Product
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "가게 상품 정보 응답 DTO")
data class ProductResponseDto(
    @Schema(description = "상품 ID")
    val productId: Long,

    @Schema(description = "상품 이름")
    val name: String,

    @Schema(description = "상품 이미지 URL")
    val imageUrl: String,

    @Schema(description = "상품 설명")
    val description: String?,

    @Schema(description = "상품 가격")
    val price: Int
) {
    companion object {
        fun from(product: Product): ProductResponseDto {
            return ProductResponseDto(
                productId = product.id,
                name = product.name,
                imageUrl = product.imageUrl,
                description = product.description,
                price = product.price
            )
        }
    }
}