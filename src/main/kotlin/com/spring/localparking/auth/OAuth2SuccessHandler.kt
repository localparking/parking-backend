package com.spring.localparking.auth

import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.auth.service.TokenService
import com.spring.localparking.global.util.CookieUtil
import com.spring.localparking.global.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException


@Component
class OAuth2SuccessHandler(
    private val jwtUtil: JwtUtil,
    private val tokenService: TokenService
) : AuthenticationSuccessHandler {

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        req: HttpServletRequest,
        res: HttpServletResponse,
        auth: Authentication
    ) {
        val principal = auth.principal as CustomPrincipal

        val userId = principal.id!!
        val userRole = principal.role

        val accessToken = jwtUtil.generateAccessToken(userId, userRole)
        val refreshToken = jwtUtil.generateRefreshToken(userId)

        tokenService.saveRefreshToken(userId, refreshToken)

        res.addHeader("Set-Cookie", CookieUtil.createAccessTokenCookie(accessToken).toString())
        res.addHeader("Set-Cookie", CookieUtil.createRefreshTokenCookie(refreshToken).toString())

        val redirectUrl = UriComponentsBuilder
            .fromUriString("http://localhost:3000/login/success")
            .queryParam("role", userRole)
            //.queryParam("accessToken", accessToken)
            //.queryParam("refreshToken", refreshToken)
            .build()
            .toUriString()

        res.sendRedirect(redirectUrl)
    }
}
