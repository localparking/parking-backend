package com.spring.localparking.user.dto

import com.spring.localparking.global.Provider
import com.spring.localparking.global.Role

data class UserResponse(
    val email: String,
    val nickname: String,
    val provider: Provider,
    val role : Role
)
