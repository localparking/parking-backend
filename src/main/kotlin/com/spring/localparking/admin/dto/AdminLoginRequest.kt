package com.spring.localparking.admin.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.annotations.NotNull

@Schema(description = "Admin 로그인 요청 DTO")
data class AdminLoginRequest(
    @field:NotNull
    @Schema(description = "관리자 ID", example = "admin")
    val adminId: String,
    @field:NotNull
    @Schema(description = "관리자 비밀번호", example = "password123")
    val password: String
)
