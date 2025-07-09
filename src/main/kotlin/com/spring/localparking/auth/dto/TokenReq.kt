package com.spring.localparking.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "토큰")
data class TokenReq(
    @field:NotBlank
    val token: String
)
