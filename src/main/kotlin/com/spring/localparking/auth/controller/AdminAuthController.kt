package com.spring.localparking.auth.controller

import com.spring.localparking.auth.dto.AdminLoginRequest
import com.spring.localparking.auth.dto.TokenResponse
import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.auth.service.TokenService
import com.spring.localparking.global.util.CookieUtil
import com.spring.localparking.global.util.JwtUtil
import io.swagger.v3.oas.annotations.Operation
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
