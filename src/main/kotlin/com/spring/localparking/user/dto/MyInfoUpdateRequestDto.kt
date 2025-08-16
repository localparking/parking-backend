package com.spring.localparking.user.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class MyInfoUpdateRequestDto(
    @field:NotBlank
    val nickname: String,

    val name: String,

    val tel: String,

    val regionName: String,

    val vehicleNumber: String,

    @field:NotNull
    val isNotification: Boolean
)