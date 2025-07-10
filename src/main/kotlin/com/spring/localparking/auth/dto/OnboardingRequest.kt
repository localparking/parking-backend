package com.spring.localparking.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "온보딩 요청 DTO")
data class OnboardingRequest(
    @Schema(description = "사용자 나이대", example = "AGE_20")
    val ageGroup: String?,
    @Schema(description = "사용자 가중치", example = "PRICE")
    val weight: String?,
    @Schema(description = "사용자 선호 카테고리 ID 목록", example = "[1, 2, 3]")
    val categoryIds: List<Long>?
)