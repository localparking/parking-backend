package com.spring.localparking.auth.dto.storekeeper

import io.swagger.v3.oas.annotations.media.Schema

data class StoreAddressDto(
    @Schema(
        description = "시도",
        example = "서울특별시"
    )
    val sido: String,
    @Schema(
        description = "시군구",
        example = "강남구"
    )
    val sigungu: String,
    @Schema(
        description = "도로명",
        example = "불정로"
    )
    val doroName: String,
    @Schema(
        description = "건물번호",
        example = "362"
    )
    val buildingNo: String,
    @Schema(
        description = "위도",
        example = "37.37227222749274"
    )
    val lat: Double,
    @Schema(
        description = "경도",
        example = "127.13733989353932"
    )
    val lon: Double
)