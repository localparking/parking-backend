package com.spring.localparking.auth.controller

import com.spring.localparking.auth.dto.AdminLoginRequest
import com.spring.localparking.auth.dto.TokenResponse
import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.auth.service.TokenService
import com.spring.localparking.global.util.CookieUtil
import com.spring.localparking.global.util.JwtUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@Tag(name = "관리자 인증 컨트롤러", description = "관리자 인증 관련 API입니다.")
@RestController
@RequestMapping("admin/auth")
class AdminAuthController(
    private val tokenService: TokenService,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil
) {
    @Operation(summary = "관리자 로그인", description = "관리자가 로그인하는 API입니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패: 잘못된 관리자 ID 또는 비밀번호"),
            ApiResponse(responseCode = "500", description = "서버 오류: 로그인 중 오류 발생"),
            ApiResponse(responseCode = "400", description = "잘못된 요청: 요청 형식이 올바르지 않음"),
            ApiResponse(responseCode = "404", description = "사용자 정보 없음: 해당 관리자를 찾을 수 없음")
        ]
    )
    @PostMapping("/login")
    fun login(@Valid @RequestBody req: AdminLoginRequest): ResponseEntity<TokenResponse> {
        val auth: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(req.adminId, req.password)
        )
        val principal = auth.principal as CustomPrincipal
        val userId = principal.id ?: throw UnauthorizedException()
        val accessToken = jwtUtil.generateAccessToken(userId, principal.role)
        val refreshToken = jwtUtil.generateRefreshToken(userId)

        tokenService.saveRefreshToken(userId, refreshToken)

        val response = TokenResponse(accessToken, refreshToken)

        return ResponseEntity.ok()
            .header("Authorization", "Bearer $accessToken")
            .header("Set-Cookie", CookieUtil.createRefreshTokenCookie(refreshToken).toString())
            .body(response)
    }
}
