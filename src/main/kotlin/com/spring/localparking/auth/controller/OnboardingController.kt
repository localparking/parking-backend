package com.spring.localparking.auth.controller

import com.spring.localparking.auth.dto.OnboardingRequest
import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.auth.service.RegisterService
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.user.dto.CategoryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "온보딩 컨트롤러", description = "온보딩 관련 API입니다.")
@RestController
@RequestMapping("/onboarding")
class OnboardingController (
    private val registerService: RegisterService
){
    @Operation(summary = "온보딩 카테고리 조회", description = "온보딩 시 카테고리를 조회하는 API입니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "카테고리 조회 성공"),
            ApiResponse(responseCode = "404", description = "카테고리 정보 없음: 해당 카테고리를 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류: 카테고리 조회 중 오류 발생")
        ]
    )
    @GetMapping("/categories")
    fun getCategories(@AuthenticationPrincipal principal: CustomPrincipal):
            ResponseEntity<CategoryResponse> {
        val response = registerService.getCategories()
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "온보딩 완료", description = "온보딩을 완료하는 API입니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "온보딩 완료 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청: 온보딩 정보가 잘못됨"),
            ApiResponse(responseCode = "404", description = "사용자 정보 없음: 해당 사용자를 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류: 온보딩 완료 중 오류 발생")
        ]
    )
    @PostMapping("/complete")
    fun completeOnboarding(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @RequestBody @Valid request: OnboardingRequest
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = requireNotNull(principal.id) { throw UnauthorizedException() }
        registerService.completeOnboarding(userId, request)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
}