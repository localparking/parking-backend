package com.spring.localparking.storekeeper.dto

import com.spring.localparking.operatingHour.DailyOperatingDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "점주에게 보여주는 연계된 주차장 정보 DTO")
data class AssociatedParkingLotResponseDto(
    @Schema(description = "주차장 코드")
    val parkingCode: String,

    @Schema(description = "주차장 이름")
    val name: String,

    @Schema(description = "주소")
    val address: String,

    @Schema(description = "전화번호")
    val tel: String? = null,

    @Schema(description = "총 주차 가능 대수")
    val capacity: Int? = null,

    @Schema(description = "운영 시간 정보")
    val operatingHours: List<DailyOperatingDto>?,

    @Schema(description = "기본 요금")
    val baseFee: Int,

    @Schema(description = "기본 시간(분)")
    val baseTimeMin: Int,

    @Schema(description = "추가 요금")
    val additionalFee: Int? = null,

    @Schema(description = "추가 시간(분)")
    val additionalTimeMin: Int? = null,

    @Schema(description = "1일 최대 요금")
    val dayPassFee: Int? = null
)