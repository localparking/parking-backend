package com.spring.localparking.auth.controller

import com.spring.localparking.auth.dto.AdminLoginRequest
import com.spring.localparking.auth.dto.TokenResponse
import com.spring.localparking.auth.dto.storekeeper.StorekeeperRegisterRequest
import com.spring.localparking.auth.service.StorekeeperRegisterService
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.global.util.CookieUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "점주 컨트롤러", description = "점주(가게주인)의 회원가입 및 로그인 관련 API입니다.")
@RestController
@RequestMapping("/storekeeper")
class StorekeeperAuthController(
    private val storekeeperService: StorekeeperRegisterService
) {
    @Operation(summary = "점주 아이디 중복 확인", description = "입력한 아이디가 사용 가능한지 확인합니다.")
    @GetMapping("/check-id")
    fun checkAdminId(@RequestParam adminId: String): ResponseEntity<ResponseDto<Boolean>> {
        val isAvailable = !storekeeperService.isAdminIdExists(adminId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, isAvailable))
    }

    @Operation(summary = "점주 회원가입", description = "가게 정보를 함께 받아 회원가입을 신청합니다.")
    @PostMapping("/register")
    fun registerStorekeeper(
        @Valid @RequestBody request: StorekeeperRegisterRequest
    ): ResponseEntity<ResponseDto<Unit>> {
        storekeeperService.registerStorekeeper(request)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.USER_CREATED))
    }

    @Operation(summary = "점주 로그인", description = "점주가 아이디와 비밀번호로 로그인하며, 심사 상태에 따라 결과가 달라집니다.")
    @PostMapping("/login")
    fun login(
        req: HttpServletRequest,
        @Valid @RequestBody body: AdminLoginRequest
    ): ResponseEntity<ResponseDto<TokenResponse>> {
        val tokenResponse = storekeeperService.loginStorekeeper(body)

        val accessCookie  = CookieUtil.createAccessTokenCookie(req, tokenResponse.accessToken!!)
        val refreshCookie = CookieUtil.createRefreshTokenCookie(req, tokenResponse.refreshToken!!)

        val headers = HttpHeaders().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer ${tokenResponse.accessToken}")
            add(HttpHeaders.SET_COOKIE, accessCookie.toString())
            add(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        }

        val responseDto = ResponseDto.from(SuccessCode.USER_LOGGED_IN, tokenResponse)
        return ResponseEntity.ok().headers(headers).body(responseDto)
    }
}
