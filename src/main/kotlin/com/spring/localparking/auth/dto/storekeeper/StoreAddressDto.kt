package com.spring.localparking.auth.dto.storekeeper

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class StoreAddressDto(
    @Schema(
        description = "시도",
        example = "서울특별시"
    )
    @field:NotBlank
    val sido: String?,
    @Schema(
        description = "시군구",
        example = "강남구"
    )
    @field:NotBlank
    val sigungu: String?,
    @Schema(
        description = "도로명",
        example = "불정로"
    )
    @field:NotBlank
    val doroName: String?,
    @Schema(
        description = "건물번호",
        example = "362"
    )
    @field:NotBlank
    val buildingNo: String?,
    @Schema(
        description = "위도",
        example = "37.4964"
    )
    @field:NotNull
    val lat: Double?,
    @Schema(
        description = "경도",
        example = "127.028"
    )
    @field:NotNull
    val lon: Double?
)