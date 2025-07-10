package com.spring.localparking.auth.dto

data class AppleSocialTokenInfoResponseDto(
    val accessToken: String,
    val expiresIn: Int,
    val idToken: String,
    val refreshToken: String,
    val tokenType: String
)