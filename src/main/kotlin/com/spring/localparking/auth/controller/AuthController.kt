package com.spring.localparking.auth.controller

import com.spring.localparking.auth.dto.TokenRequest
import com.spring.localparking.auth.dto.TokenResponse
import com.spring.localparking.auth.dto.social.AppleLoginRequest
import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.auth.service.TokenService
import com.spring.localparking.auth.service.social.SocialAuthService
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.global.util.CookieUtil
import com.spring.localparking.global.util.JwtUtil
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "인증 컨트롤러", description = "인증 관련 API입니다.")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil,
    private val socialAuthService: SocialAuthService,
    private val tokenService: TokenService
) {

    @Operation(summary = "Access Token 갱신", description = "Access Token을 갱신하는 API입니다.")
    @PostMapping("/refresh")
    fun reissueRefreshToken(
        req: HttpServletRequest,
        @AuthenticationPrincipal principal: CustomPrincipal
    ): ResponseEntity<ResponseDto<TokenResponse>> {
        val userId = requireNotNull(principal.id) { throw UnauthorizedException() }
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        val accessToken = jwtUtil.generateAccessToken(userId, user.role.value)
        val refreshToken = jwtUtil.generateRefreshToken(userId)
        tokenService.renewRefreshToken(userId, refreshToken)

        val accessCookie  = CookieUtil.createAccessTokenCookie(req, accessToken)
        val refreshCookie = CookieUtil.createRefreshTokenCookie(req, refreshToken)

        val headers = HttpHeaders().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            add(HttpHeaders.SET_COOKIE, accessCookie.toString())
            add(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        }

        val body = ResponseDto.from(
            SuccessCode.OK,
            TokenResponse(accessToken, refreshToken)
        )

        return ResponseEntity.ok().headers(headers).body(body)
    }

    @Operation(summary = "카카오 앱 소셜 로그인", description = "카카오 앱 소셜 로그인을 위한 API입니다.")
    @PostMapping("/login/kakao")
    fun kakao(
        req: HttpServletRequest,
        @RequestBody @Valid request: TokenRequest
    ): ResponseEntity<ResponseDto<TokenResponse>> {
        val user = socialAuthService.loginKakao(request.token)

        val accessToken = jwtUtil.generateAccessToken(user.id!!, user.role.value)
        val refreshToken = jwtUtil.generateRefreshToken(user.id!!)
        tokenService.saveRefreshToken(user.id!!, refreshToken)

        val accessCookie  = CookieUtil.createAccessTokenCookie(req, accessToken)
        val refreshCookie = CookieUtil.createRefreshTokenCookie(req, refreshToken)

        val headers = HttpHeaders().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            add(HttpHeaders.SET_COOKIE, accessCookie.toString())
            add(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        }

        val res = TokenResponse(accessToken, refreshToken)
        return ResponseEntity.ok().headers(headers)
            .body(ResponseDto.from(SuccessCode.USER_LOGGED_IN, res))
    }

    @Operation(summary = "애플 앱 소셜 로그인", description = "애플 앱 소셜 로그인을 위한 API입니다.")
    @PostMapping("/login/apple")
    fun apple(
        req: HttpServletRequest,
        @RequestBody @Valid request: AppleLoginRequest
    ): ResponseEntity<ResponseDto<TokenResponse>> {
        val user = socialAuthService.loginApple(request)

        val accessToken = jwtUtil.generateAccessToken(user.id!!, user.role.value)
        val refreshToken = jwtUtil.generateRefreshToken(user.id!!)
        tokenService.saveRefreshToken(user.id!!, refreshToken)

        val accessCookie  = CookieUtil.createAccessTokenCookie(req, accessToken)
        val refreshCookie = CookieUtil.createRefreshTokenCookie(req, refreshToken)

        val headers = HttpHeaders().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            add(HttpHeaders.SET_COOKIE, accessCookie.toString())
            add(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        }

        val res = TokenResponse(accessToken, refreshToken)
        return ResponseEntity.ok().headers(headers)
            .body(ResponseDto.from(SuccessCode.USER_LOGGED_IN, res))
    }

    @PostMapping("/logout")
    fun logout(
        req: HttpServletRequest,
        @AuthenticationPrincipal principal: CustomPrincipal
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = requireNotNull(principal.id) { throw UnauthorizedException() }
        tokenService.deleteRefreshToken(userId)

        val accessDel  = CookieUtil.deleteCookie(req, "accessToken")
        val refreshDel = CookieUtil.deleteCookie(req, "refreshToken")

        val headers = HttpHeaders().apply {
            add(HttpHeaders.SET_COOKIE, accessDel.toString())
            add(HttpHeaders.SET_COOKIE, refreshDel.toString())
        }

        return ResponseEntity.ok().headers(headers)
            .body(ResponseDto.empty(SuccessCode.OK))
    }
}
