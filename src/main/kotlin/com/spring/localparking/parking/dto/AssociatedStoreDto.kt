package com.spring.localparking.parking.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "주차장과 연계된 가게 정보 DTO")
data class AssociatedStoreDto(
    @Schema(description = "가게 ID")
    val storeId: Long,

    @Schema(description = "가게 세부 카테고리 이름", example = "한식")
    val categoryName: String,

    @Schema(description = "가게 이름", example = "종로 할머니 칼국수")
    val storeName: String,

    @Schema(description = "현재 가게 영업 여부 (true: 영업 중, false: 영업 종료, null: 정보 없음)")
    val isOpen: Boolean?
)