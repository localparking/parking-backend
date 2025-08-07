package com.spring.localparking.auth.dto.storekeeper

import com.spring.localparking.auth.dto.join.AgreementDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "점주 회원가입 요청 DTO")
data class StorekeeperRegisterRequest(
    @Schema(description = "점주가 동의한 약관 목록", required = true)
    val agreements: List<AgreementDto>,
    @Schema(description = "점주 아이디", required = true, example = "storekeeper123")
    val adminId: String,
    @Schema(description = "점주 비밀번호", required = true, example = "password123")
    val password: String,
    @Schema(description = "점주 이메일", required = true, example = "123@gmail.com")
    val email: String,
    @Schema(description = "사업자번호", required = true, example = "12345-67890")
    val businessNumber: String,
    @Schema(description = "기존 가게를 선택한 경우 가게 ID", example = "101")
    val storeId: Long? = null,
    @Schema(description = "가게 이름", example = "맛있는 가게")
    val storeName: String? = null,
    @Schema(description = "가게 주소")
    val storeAddress: StoreAddressDto? = null,
    @Schema(description = "가게 카테고리", example = "1")
    val categoryId: Long? = null
)