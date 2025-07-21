package com.spring.localparking.auth.controller

import com.spring.localparking.auth.dto.join.RegisterRequest
import com.spring.localparking.auth.dto.join.TermsResponse
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

@Tag(name = "가입 컨트롤러", description = "가입 관련 API입니다.")
@RestController
@RequestMapping("/register")
class RegisterController (
    private val registerService: RegisterService
){
    @Operation(summary = "약관 조회", description = "회원가입 시 약관을 조회하는 API입니다.")
    @GetMapping("/terms")
    fun getTerms(@AuthenticationPrincipal principal: CustomPrincipal): ResponseEntity<ResponseDto<TermsResponse>> {
        val response = registerService.getTerms()
        return ResponseEntity.ok(
            ResponseDto.from(
            SuccessCode.OK, response)
        )
    }

    @Operation(summary = "회원가입- 약관동의", description = "회원가입 시 약관 동의를 위한 API입니다.")
    @PostMapping("/agreements")
    fun registerAgreements(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @RequestBody @Valid request: RegisterRequest
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = requireNotNull(principal.id) { throw UnauthorizedException() }
        registerService.registerAgreements(userId, request)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
}