package com.spring.localparking.user.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class MyInfoUpdateRequestDto(
    @field:NotBlank
    val nickname: String,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val tel: String,

    @field:NotBlank
    val regionName: String,

    @field:NotBlank
    val vehicleNumber: String,

    @field:NotNull
    val isNotification: Boolean
)