package com.spring.localparking.global.util

import org.springframework.http.ResponseCookie

object CookieUtil {

    private const val REFRESH_TOKEN_EXPIRY = 60 * 60 * 24 * 7L
    private const val ACCESS_TOKEN_EXPIRY = 60 * 15L // 15분
    private val aDomain = ".townparking.store"

    fun createAccessTokenCookie(accessToken: String, maxAge: Long = ACCESS_TOKEN_EXPIRY): ResponseCookie =
        ResponseCookie.from("accessToken", accessToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(maxAge)
            .sameSite("None")
            .domain(aDomain)
            .build()

    fun createRefreshTokenCookie(refreshToken: String, maxAge: Long = REFRESH_TOKEN_EXPIRY) : ResponseCookie =
        ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(maxAge)
            .sameSite("None")
            .domain(aDomain)
            .build()

}