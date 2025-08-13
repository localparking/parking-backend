package com.spring.localparking.user.controller

import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.global.util.CookieUtil
import com.spring.localparking.user.dto.UserInfoResponse
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import com.spring.localparking.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "유저 컨트롤러", description = "유저 관련 API입니다.")
@Controller
@RequestMapping("/user")
class UserController (
    private val userRepository: UserRepository,
    private val userService: UserService
){
    @Operation(summary = "내 정보 조회", description = "로그인된 사용자 본인의 정보를 조회합니다.")
    @GetMapping("/my-info")
    fun getMyInfo(
        @AuthenticationPrincipal principal: CustomPrincipal?
    ): ResponseEntity<ResponseDto<UserInfoResponse>> {
        val userId = principal?.id ?: throw UnauthorizedException()
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        val userInfoResponse = UserInfoResponse.from(user)
        return ResponseEntity.ok(
            ResponseDto.from(SuccessCode.OK, userInfoResponse)
        )
    }
    @Operation(summary = "회원 탈퇴", description = "로그인된 사용자의 계정을 탈퇴 처리합니다.")
    @PostMapping("/withdraw")
    fun withdrawUser(
        req: HttpServletRequest,
        @AuthenticationPrincipal principal: CustomPrincipal?
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal?.id ?: throw UnauthorizedException()

        userService.withdrawUser(userId)

        val accessDel   = CookieUtil.deleteCookie(req, "accessToken")
        val refreshDel  = CookieUtil.deleteCookie(req, "refreshToken")

        val headers = HttpHeaders().apply {
            add(HttpHeaders.SET_COOKIE, accessDel.toString())
            add(HttpHeaders.SET_COOKIE, refreshDel.toString())
        }
        return ResponseEntity.ok()
            .headers(headers)
            .body(ResponseDto.empty(SuccessCode.OK))
    }
}