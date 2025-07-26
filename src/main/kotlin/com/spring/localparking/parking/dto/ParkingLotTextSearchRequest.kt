package com.spring.localparking.parking.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "텍스트 기반 주차장 검색 요청 DTO")
data class ParkingLotTextSearchRequest(
    @Schema(description = "현재 위치 위도", example = "37.498095")
    val lat: Double? = null,
    @Schema(description = "현재 위치 경도", example = "127.027610")
    val lon: Double?= null,
    @Schema(description = "검색어", example = "강남역 주차장")
    val query: String,
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    val page: Int = 0
)