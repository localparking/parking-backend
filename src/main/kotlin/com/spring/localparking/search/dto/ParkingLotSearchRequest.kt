package com.spring.localparking.search.dto

import com.spring.localparking.global.dto.SortType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "지도 기반 주차장 검색 요청 DTO")
data class ParkingLotSearchRequest(
    @Schema(description = "검색 반경 레벨 (1: 2km, 2: 4km)", example = "1", defaultValue = "1")
    val distanceLevel: Int = 1,
    @Schema(description = "위도", example = "37.498095")
    val lat: Double?= null,
    @Schema(description = "경도", example = "127.027610")
    val lon: Double?= null,
    @Schema(description = "검색어 (텍스트 검색 시 사용)", example = "강남역 주차장")
    val query: String? = null,
    @Schema(description = "검색 정렬 (예: DISTANCE, PRICE)", example = "DISTANCE")
    val sort: SortType?,
    @Schema(description = "무료 여부", example = "false")
    val isFree: Boolean?,
    @Schema(description = "실시간 주차 가능 여부", example = "false")
    val isRealtime: Boolean?,
    @Schema(description = "주차장 혼잡도 (예: 여유, 보통, 혼잡)", example = "[\"여유\", \"보통\"]")
    val congestion: List<String>?,
    @Schema(description = "최대 시간당 요금 (단위: 원)", example = "10000")
    val maxFeePerHour: Int?,
    @Schema(description = "운영 여부", example = "true")
    val isOpen: Boolean?,
    @Schema(description = "24시간 운영 여부", example = "false")
    val is24Hours: Boolean?,
    @Schema(description = "확인할 요일 (대문자, 예: MONDAY)", example = "MONDAY")
    val checkDayOfWeek: String? = null,
    @Schema(description = "확인할 시간 (HHmm 형식, 예: 1430)", example = "1430")
    val checkTime: String? = null,
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    val page: Int = 0
)