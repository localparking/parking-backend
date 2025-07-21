package com.spring.localparking.store.dto

import com.spring.localparking.global.dto.SortType
import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.annotations.NotNull


@Schema(description = "지도 기반 가게 검색 요청 DTO")
data class StoreSearchRequest(
    @field:NotNull
    @Schema(description = "위도", example = "37.498095")
    val lat: Double,
    @field:NotNull
    @Schema(description = "경도", example = "127.027610")
    val lon: Double,
    @field:NotNull
    @Schema(description = "카테고리 ID", example = "2")
    val categoryId: Long?= null,
    @Schema(description = "검색 정렬 (예: DISTANCE)", example = "DISTANCE")
    val sort: SortType = SortType.DISTANCE,
    @Schema(description = "최대 무료 주차(분)", example = "30")
    val maxFreeMin: Int? = null,
    @Schema(description = "운영 여부", example = "true")
    val isOpen: Boolean?= null,
    @Schema(description = "24시간 운영 여부", example = "false")
    val is24Hours: Boolean?= null,
    @Schema(description = "확인할 요일 (대문자, 예: MONDAY)", example = "MONDAY")
    val checkDayOfWeek: String? = null,
    @Schema(description = "확인할 시간 (HHmm 형식, 예: 1430)", example = "1430")
    val checkTime: String? = null,
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    val page: Int = 0
)