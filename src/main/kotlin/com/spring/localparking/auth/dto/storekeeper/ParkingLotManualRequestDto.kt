package com.spring.localparking.auth.dto.storekeeper

import com.spring.localparking.operatingHour.DailyOperatingDto
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "점주가 수동으로 입력하는 주차장 정보 DTO")
data class ParkingLotManualRequestDto(
    @Schema(description = "주차장 이름", example = "서울시청 주차장")
    @field:NotBlank
    val name: String?,
    @Schema(description = "주소", example = "서울특별시 중구 세종대로 110")
    @field:NotBlank
    val address: String?,
    @Schema(description = "전화번호", example = "02-123-4567")
    val tel: String?=null,
    @Schema(description = "총 주차 가능 대수", example = "100")
    val capacity: Int? = null,
    @Schema(description = "운영 시간 정보 (null 가능)", required = false)
    val operatingHours: List<DailyOperatingDto>? = null,
    @Schema(description = "기본 요금", example = "2000")
    @field:NotNull
    val baseFee: Int?,
    @Schema(description = "기본 시간(분)", example = "60")
    @field:NotNull
    val baseTimeMin: Int?,
    @Schema(description = "추가 요금", example = "500")
    val additionalFee: Int?=null,
    @Schema(description = "추가 시간(분)", example = "30")
    val additionalTimeMin: Int?=null,
    @Schema(description = "1일 최대 요금", example = "20000")
    val dayPassFee: Int? = null,
    @Schema(description = "위도", example = "37.5665")
    @field:NotNull
    val lat : Double?,
    @Schema(description = "경도", example = "126.978")
    @field:NotNull
    val lon : Double?,
)