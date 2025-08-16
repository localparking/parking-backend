package com.spring.localparking.user.dto

import com.spring.localparking.global.dto.Provider
import com.spring.localparking.global.dto.Role
import com.spring.localparking.user.domain.User

data class MyInfoResponseDto(
    val email: String,
    val nickname: String,
    val provider: Provider,
    val role : Role,
    val name: String?,
    val tel: String?,
    val regionName: String?,
    val vehicleNumber: String?,
    val isOnboarding: Boolean,
    val isNotification: Boolean,
){
    companion object {
        fun from(user: User) = MyInfoResponseDto(
            email = user.email,
            nickname = user.nickname,
            provider = user.provider,
            role = user.role,
            isOnboarding = user.isOnboarding,
            isNotification = user.isNotification,
            name = user.userProfile?.name,
            tel = user.userProfile?.tel,
            regionName = user.userProfile?.regionName,
            vehicleNumber = user.userProfile?.vehicleNumber,
        )
    }
}
