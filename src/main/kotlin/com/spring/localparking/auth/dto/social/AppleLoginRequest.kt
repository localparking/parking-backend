package com.spring.localparking.auth.dto.social

data class AppleLoginRequest(
    val identityToken: String,
    val fullName: String?
)