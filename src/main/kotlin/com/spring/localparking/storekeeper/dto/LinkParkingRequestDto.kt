package com.spring.localparking.storekeeper.dto

import com.spring.localparking.auth.dto.storekeeper.ParkingLotManualRequestDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "주차장 연계 요청 DTO")
data class LinkParkingRequestDto(
    @Schema(description = "연결할 기존 주차장의 코드 (새로운 주차장 등록시 null)", example = "100-2-000228")
    val parkingCode: String?,

    @Schema(description = "수동으로 입력할 새로운 주차장 정보")
    val parkingLotInfo: ParkingLotManualRequestDto?
)