package com.spring.localparking.user.controller

import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.user.dto.UserResponse
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "유저 컨트롤러", description = "유저 관련 API입니다.")
@Controller
@RequestMapping("/user")
class UserController (
    private val userRepository: UserRepository
){
    @Operation(summary = "내 정보 조회", description = "로그인된 사용자 본인의 정보를 조회합니다.")
    @GetMapping("/my-info")
    fun getMyInfo(
        @AuthenticationPrincipal principal: CustomPrincipal?
    ): ResponseEntity<ResponseDto<UserResponse>> {
        val userId = principal?.id ?: throw UnauthorizedException()
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        val userResponse = UserResponse.from(user)
        return ResponseEntity.ok(
            ResponseDto.from(SuccessCode.OK, userResponse)
        )
    }
}