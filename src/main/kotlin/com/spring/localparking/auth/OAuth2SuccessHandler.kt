package com.spring.localparking.auth

import com.spring.localparking.auth.component.CookieAuthorizationRequestRepository
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.auth.service.TokenService
import com.spring.localparking.global.util.CookieUtil
import com.spring.localparking.global.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler // 1. 상속 클래스 변경
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

@Component
class OAuth2SuccessHandler(
    private val jwtUtil: JwtUtil,
    private val tokenService: TokenService,
    private val cookieAuthorizationRequestRepository: CookieAuthorizationRequestRepository
) : SimpleUrlAuthenticationSuccessHandler() {

    private val allowedRedirectUris = setOf(
        "http://localhost:3001/login/success",
        "https://townparking.store/login/success"
    )

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        req: HttpServletRequest,
        res: HttpServletResponse,
        auth: Authentication
    ) {
        val targetUrl = determineTargetUrl(req, res, auth)

        if (res.isCommitted) {
            logger.debug("Response has already been committed. Unable to redirect to $targetUrl")
            return
        }

        val principal = auth.principal as CustomPrincipal
        val userId = principal.id!!
        val userRole = principal.role

        val accessToken = jwtUtil.generateAccessToken(userId, userRole)
        val refreshToken = jwtUtil.generateRefreshToken(userId)

        tokenService.saveRefreshToken(userId, refreshToken)

        res.addHeader("Set-Cookie", CookieUtil.createAccessTokenCookie(accessToken).toString())
        res.addHeader("Set-Cookie", CookieUtil.createRefreshTokenCookie(refreshToken).toString())

        clearAuthenticationAttributes(req)
        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(req, res)

        val redirectUrl = UriComponentsBuilder
            .fromUriString(targetUrl)
            .queryParam("role", userRole)
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build()
            .toUriString()

        redirectStrategy.sendRedirect(req, res, redirectUrl)
    }

    override fun determineTargetUrl(
        req: HttpServletRequest,
        res: HttpServletResponse,
        auth: Authentication
    ): String {
        val redirectUri = CookieUtil.getCookie(req, CookieAuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
            ?.value
        if (!redirectUri.isNullOrBlank() && isAuthorizedRedirectUri(redirectUri)) {
            return redirectUri
        }
        return "https://townparking.store/login/success"
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        return allowedRedirectUris.contains(uri)
    }
}