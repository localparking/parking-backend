package com.spring.localparking.auth.controller


import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.auth.service.TokenService
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.global.util.JwtUtil
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "인증 컨트롤러", description = "인증 관련 API입니다.")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val tokenService: TokenService,
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {
    @Operation(summary = "Access Token 갱신", description = "Access Token을 갱신하는 API입니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Access Token 갱신 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패: 유효하지 않은 토큰"),
            ApiResponse(responseCode = "404", description = "사용자 정보 없음: 해당 사용자를 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류: 토큰 갱신 중 오류 발생")
        ]
    )
    @PostMapping("/refresh")
    fun refreshAccessToken(
        @AuthenticationPrincipal principal: CustomPrincipal
    ): ResponseEntity<ResponseDto<Map<String, String>>> {
        val userId = requireNotNull(principal.id) {throw UnauthorizedException()}
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        val accessToken = jwtUtil.generateAccessToken(userId, user.role.value)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, mapOf("accessToken" to accessToken)))
    }

    @Operation(summary = "Refresh Token 재발급", description = "Refresh Token을 재발급하는 API입니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Refresh Token 재발급 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패: 유효하지 않은 토큰"),
            ApiResponse(responseCode = "404", description = "사용자 정보 없음: 해당 사용자를 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류: 토큰 재발급 중 오류 발생")
        ]
    )
    @PostMapping("/reissue-refresh")
    fun reissueRefreshToken(
        @AuthenticationPrincipal principal: CustomPrincipal
    ): ResponseEntity<ResponseDto<Map<String, String>>> {
        val userId = requireNotNull(principal.id) {throw UnauthorizedException()}

        val refreshToken = jwtUtil.generateRefreshToken(userId)
        tokenService.renewRefreshToken(userId, refreshToken)

        return ResponseEntity.ok(
            ResponseDto.from(SuccessCode.OK, mapOf("refreshToken" to refreshToken))
        )
    }
}
