package com.spring.localparking.auth.controller


import com.spring.localparking.auth.dto.TokenRequest
import com.spring.localparking.auth.dto.TokenResponse
import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.auth.service.TokenService
import com.spring.localparking.auth.service.social.SocialAuthService
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.global.util.JwtUtil
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
        @AuthenticationPrincipal principal: CustomPrincipal
    ): ResponseEntity<ResponseDto<TokenResponse>> {
        val userId = requireNotNull(principal.id) { throw UnauthorizedException() }
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        val accessToken = jwtUtil.generateAccessToken(userId, user.role.value)
        val refreshToken = jwtUtil.generateRefreshToken(userId)
        tokenService.renewRefreshToken(userId, refreshToken)
        val headers = HttpHeaders().apply {
            add(HttpHeaders.SET_COOKIE, accessToken)
            add(HttpHeaders.SET_COOKIE, refreshToken)
        }
        val body = ResponseDto.from(SuccessCode.OK,
            TokenResponse(accessToken, refreshToken)
        )
        return ResponseEntity
            .ok()
            .headers(headers)
            .body(body)
    }

    @Operation(summary = "카카오 앱 소셜 로그인", description = "카카오 앱 소셜 로그인을 위한 API입니다.")
    @PostMapping("/login/kakao")
    fun kakao(@RequestBody @Valid req: TokenRequest
    ): ResponseEntity<ResponseDto<TokenResponse>> {
        val user = socialAuthService.loginKakao(req.token)
        val accessToken = jwtUtil.generateAccessToken(user.id!!, user.role.value)
        val refreshToken = jwtUtil.generateRefreshToken(user.id!!)
        tokenService.saveRefreshToken(user.id!!, refreshToken)
        val res = TokenResponse(accessToken, refreshToken)
        return ResponseEntity.ok(
            ResponseDto.from(SuccessCode.USER_LOGGED_IN, res)
        )
    }

    @Operation(summary = "애플 앱 소셜 로그인", description = "애플 앱 소셜 로그인을 위한 API입니다.")
    @PostMapping("/login/apple")
    fun apple(@RequestBody @Valid req: TokenRequest
    ): ResponseEntity<ResponseDto<TokenResponse>> {
        val user = socialAuthService.loginApple(req.token)
        val accessToken = jwtUtil.generateAccessToken(user.id!!, user.role.value)
        val refreshToken = jwtUtil.generateRefreshToken(user.id!!)
        tokenService.saveRefreshToken(user.id!!, refreshToken)
        val res = TokenResponse(accessToken, refreshToken)
        return ResponseEntity.ok(
            ResponseDto.from(SuccessCode.USER_LOGGED_IN, res)
        )
    }

    @Operation(summary = "게스트 로그인", description = "게스트 로그인을 위한 API입니다.")
    @PostMapping("/login/guest")
    fun guestLogin(): ResponseEntity<ResponseDto<TokenResponse>> {
        val guestUser = socialAuthService.loginAsGuest()
        val accessToken = jwtUtil.generateAccessToken(guestUser.id!!, guestUser.role.value)
        val refreshToken = jwtUtil.generateRefreshToken(guestUser.id!!)
        tokenService.saveRefreshToken(guestUser.id!!, refreshToken)
        val response = TokenResponse(accessToken, refreshToken)
        return ResponseEntity.ok(
            ResponseDto.from(SuccessCode.USER_LOGGED_IN, response)
        )
    }
}
