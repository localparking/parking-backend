package com.spring.localparking.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.annotations.NotNull

@Schema(description = "토큰 응답 DTO")
data class TokenResponse(
    @field:NotNull
    val accessToken: String,

    @field:NotNull
    val refreshToken: String
)
