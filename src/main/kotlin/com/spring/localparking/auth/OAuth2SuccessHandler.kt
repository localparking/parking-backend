package com.spring.localparking.auth

import com.spring.localparking.auth.component.CookieAuthorizationRequestRepository
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
import org.springframework.web.util.WebUtils.getCookie
import java.io.IOException


@Component
class OAuth2SuccessHandler(
    private val jwtUtil: JwtUtil,
    private val tokenService: TokenService,
    private val cookieAuthorizationRequestRepository: CookieAuthorizationRequestRepository
) : AuthenticationSuccessHandler {

    private val defaultRedirectUrl = "http://localhost:3001/login/success"

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        req: HttpServletRequest,
        res: HttpServletResponse,
        auth: Authentication,

    ) {
        val targetUrl = getCookie(req, CookieAuthorizationRequestRepository().REDIRECT_URI_PARAM_COOKIE_NAME)
            ?.value ?: defaultRedirectUrl

        val principal = auth.principal as CustomPrincipal

        val userId = principal.id!!
        val userRole = principal.role

        val accessToken = jwtUtil.generateAccessToken(userId, userRole)
        val refreshToken = jwtUtil.generateRefreshToken(userId)

        tokenService.saveRefreshToken(userId, refreshToken)
        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(req, res)

        res.addHeader("Set-Cookie", CookieUtil.createAccessTokenCookie(accessToken).toString())
        res.addHeader("Set-Cookie", CookieUtil.createRefreshTokenCookie(refreshToken).toString())

//        val uriBuilder = UriComponentsBuilder.fromUriString(targetUrl)
//            .queryParam("role", userRole)
//
//        if (targetUrl.contains("localhost")) {
//            uriBuilder.queryParam("accessToken", accessToken)
//            uriBuilder.queryParam("refreshToken", refreshToken)
//        }

//        val redirectUrl = uriBuilder.build().toUriString()
//        res.sendRedirect(redirectUrl)
        val redirectUrl = UriComponentsBuilder
            .fromUriString(targetUrl)
            .queryParam("role", userRole)
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build()
            .toUriString()

        res.sendRedirect(redirectUrl)
    }
    private fun getCookie(request: HttpServletRequest, name: String) =
        request.cookies?.find { it.name == name }
}
