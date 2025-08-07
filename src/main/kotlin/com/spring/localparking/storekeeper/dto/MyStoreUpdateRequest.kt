package com.spring.localparking.storekeeper.dto

import com.spring.localparking.auth.dto.storekeeper.StoreAddressDto
import com.spring.localparking.operatingHour.DailyOperatingDto
import io.swagger.v3.oas.annotations.media.Schema

data class MyStoreUpdateRequest(
    @Schema(description = "변경할 카테고리 ID")
    val categoryId: Long,

    @Schema(description = "변경할 전화번호")
    val tel: String?,

    @Schema(description = "변경할 주소 정보")
    val address: StoreAddressDto,

    @Schema(description = "변경할 영업시간 정보")
    val operatingHours: List<DailyOperatingDto>
)