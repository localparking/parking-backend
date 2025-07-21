package com.spring.localparking.auth.controller

import com.spring.localparking.auth.dto.OnboardingRequest
import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.auth.service.RegisterService
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import io.swagger.v3.oas.annotations.Operation
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
    @Operation(summary = "온보딩 완료", description = "온보딩을 완료하는 API입니다.")
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