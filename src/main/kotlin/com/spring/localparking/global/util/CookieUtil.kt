package com.spring.localparking.global.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseCookie

object CookieUtil {

    private const val REFRESH_TOKEN_EXPIRY = 60 * 60 * 24 * 7L
    private const val ACCESS_TOKEN_EXPIRY = 60 * 15L // 15분

    fun getCookie(request: HttpServletRequest, name: String): Cookie? {
        return request.cookies?.find { it.name == name }
    }

    private fun resolveCookieDomain(req: HttpServletRequest): String? {
        val rawHost = req.getHeader("X-Forwarded-Host")?.split(",")?.first()?.trim()
            ?: req.serverName
        val host = rawHost.substringBefore(":")
        val isIp = Regex("""^\d{1,3}(\.\d{1,3}){3}$""").matches(host)

        if (host.equals("localhost", true) || isIp) return null

        return when {
            host == "townparking.store" -> "townparking.store"
            host.endsWith(".townparking.store") -> "townparking.store"
            else -> null // 기타 도메인은 host-only 권장
        }
    }
    fun createAccessTokenCookie(req: HttpServletRequest, accessToken: String, maxAge: Long = ACCESS_TOKEN_EXPIRY): ResponseCookie {
        val domain = resolveCookieDomain(req)
        val b = ResponseCookie.from("accessToken", accessToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(maxAge)
        if (domain != null) b.domain(domain)
        return b.build()
    }

    fun createRefreshTokenCookie(req: HttpServletRequest, refreshToken: String, maxAge: Long = REFRESH_TOKEN_EXPIRY): ResponseCookie {
        val domain = resolveCookieDomain(req)
        val b = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(maxAge)
        if (domain != null) b.domain(domain)
        return b.build()
    }

    fun deleteCookie(req: HttpServletRequest, name: String): ResponseCookie {
        val domain = resolveCookieDomain(req)
        val b = ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(0)
        if (domain != null) b.domain(domain)
        return b.build()
    }
}