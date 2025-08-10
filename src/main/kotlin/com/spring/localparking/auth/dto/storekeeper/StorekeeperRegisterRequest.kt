package com.spring.localparking.auth.dto.storekeeper

import com.spring.localparking.auth.dto.join.AgreementDto
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

@Schema(description = "점주 회원가입 요청 DTO")
data class StorekeeperRegisterRequest(
    @Schema(description = "점주가 동의한 약관 목록", required = true)
    val agreements: List<AgreementDto>,
    @Schema(description = "점주 아이디", required = true, example = "storekeeper123")
    @field:NotBlank
    val adminId: String,
    @Schema(description = "점주 비밀번호", required = true, example = "password123")
    @field:NotBlank
    val password: String,
    @Schema(description = "점주 이메일", required = true, example = "123@gmail.com")
    @field:NotBlank
    val email: String,
    @Schema(description = "사업자번호", required = true, example = "12345-67890")
    @field:NotBlank
    val businessNumber: String,
    @Schema(description = "가게 ID (신규 등록 시에는 null)", example = "101")
    val storeId: Long? = null,
    @Schema(description = "수동으로 입력한 가게 정보 (기존 가게 선택 시에는 null)")
    @field:Valid
    val storeInfo: StoreManualRequestDto? = null,
    @Schema(description = "선택된 기존 주차장 코드 (신규 등록 시에는 null)")
    val parkingCode: String? = null,
    @Schema(description = "수동으로 입력한 주차장 정보 (기존 주차장 선택 시에는 null)")
    @field:Valid
    val parkingLotInfo: ParkingLotManualRequestDto? = null
)