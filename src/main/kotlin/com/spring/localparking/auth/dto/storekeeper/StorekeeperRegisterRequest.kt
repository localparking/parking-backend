package com.spring.localparking.auth.dto.storekeeper

import com.spring.localparking.auth.dto.join.AgreementDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "점주 회원가입 요청 DTO")
data class StorekeeperRegisterRequest(
    @Schema(description = "점주가 동의한 약관 목록", required = true)
    val agreements: List<AgreementDto>,
    @Schema(description = "점주 아이디", required = true)
    val adminId: String,
    @Schema(description = "점주 비밀번호", required = true)
    val password: String,
    @Schema(description = "점주 이메일", required = true)
    val email: String,
    @Schema(description = "사업자번호", required = true)
    val businessNumber: String,
    @Schema(description = "가게 이름", required = true)
    val storeName: String,
    @Schema(description = "가게 주소", required = true)
    val storeAddress: StoreAddressDto,
    @Schema(description = "가게 카테고리", required = true)
    val categoryId: Long
)