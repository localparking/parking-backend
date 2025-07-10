package com.spring.localparking.auth.dto.join

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "약관 동의 DTO")
data class AgreementDto(
    @Schema(description = "약관 ID", example = "1")
    val termId: Long,
    @Schema(description = "약관 동의 여부", example = "true")
    val agreed: Boolean
)