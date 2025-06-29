package com.spring.localparking.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 응답 DTO")
data class TokenResponse(
    val accessToken: String?,

    val refreshToken: String?
)
