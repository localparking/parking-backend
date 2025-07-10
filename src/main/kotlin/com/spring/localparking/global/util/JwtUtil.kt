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
        private const val ACCESS_EXP  = 15 * 60 * 1000L        // 15분
        private const val REFRESH_EXP = 7  * 24 * 60 * 60 * 1000L // 7일
    }

    /* 기존: 회원용 */
    fun generateAccessToken(userId: Long, role: String): String =
        buildToken(userId.toString(), role, ACCESS_EXP)

    fun generateRefreshToken(userId: Long): String =
        buildToken(userId.toString(), null, REFRESH_EXP)

    /* 추가: 게스트·테스트용 */
    fun generateAccessToken(sub: String, role: String): String =
        buildToken(sub, role, ACCESS_EXP)

    fun generateRefreshToken(sub: String): String =
        buildToken(sub, null, REFRESH_EXP)

    private fun buildToken(subject: String, role: String?, exp: Long): String =
        Jwts.builder()
            .setSubject(subject)
            .apply { role?.let { claim("role", it) } }
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + exp))
            .signWith(key)
            .compact()

    fun parse(token: String): Claims =
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body
}
