package com.spring.localparking.global.util

import jakarta.servlet.http.Cookie
import org.springframework.http.ResponseCookie

object CookieUtil {

    private const val REFRESH_TOKEN_EXPIRY = 60 * 60 * 24 * 7L

    fun createAccessTokenCookie(accessToken: String): ResponseCookie =
        ResponseCookie.from("accessToken", accessToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(60 * 15)
            .sameSite("None")
            .build()

    fun createRefreshTokenCookie(refreshToken: String) : ResponseCookie =
        ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(REFRESH_TOKEN_EXPIRY)
            .sameSite("None")
            .build()

    fun deleteRefreshTokenCookie() : Cookie =
        Cookie("refreshToken", null).apply {
            maxAge = 0
            isHttpOnly = true
            secure = true
            path = "/"
            domain = "localhost"
        }
}