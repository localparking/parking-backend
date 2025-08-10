package com.spring.localparking.auth.dto.storekeeper

import com.spring.localparking.operatingHour.DailyOperatingDto
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "점주가 수동으로 입력하는 가게 정보 DTO")
class StoreManualRequestDto (
    @Schema(description = "가게 이름", example = "맛있는 가게", required = true)
    @field:NotBlank
    val storeName: String?,

    @Schema(description = "가게 주소", required = true)
    @field:Valid
    @field:NotNull
    val storeAddress: StoreAddressDto,

    @Schema(description = "전화번호", example = "02-123-4567")
    val tel: String? = null,

    @Schema(description = "가게 카테고리 ID", required = true)
    @field:NotNull
    val categoryId : Long?,

    @Schema(description = "운영 시간 정보 (null 가능)", required = false)
    val operatingHours: List<DailyOperatingDto>? = null

)