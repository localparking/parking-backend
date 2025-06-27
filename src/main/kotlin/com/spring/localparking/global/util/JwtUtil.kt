package com.spring.localparking.global.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class JwtUtil(private val jwt: JwtProperties) {
    private val key = Keys.hmacShaKeyFor(jwt.secret.toByteArray(StandardCharsets.UTF_8))

    companion object {
        private const val ACCESS_EXP: Long = 15 * 60 * 1000L
        private const val REFRESH_EXP: Long = 7 * 24 * 60 * 60 * 1000L
    }

    fun generateAccessToken(id: Long, role: String): String =
        Jwts.builder()
            .subject(id.toString())
            .claim("role", role)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + ACCESS_EXP))
            .signWith(key)
            .compact()

    fun generateRefreshToken(id: Long): String =
        Jwts.builder()
            .subject(id.toString())
            .expiration(Date(System.currentTimeMillis() + REFRESH_EXP))
            .signWith(key)
            .compact()

    fun parse(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}