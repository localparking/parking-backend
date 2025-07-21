package com.spring.localparking.user.dto

import com.spring.localparking.global.dto.Provider
import com.spring.localparking.global.dto.Role
import com.spring.localparking.user.domain.User

data class UserResponse(
    val email: String,
    val nickname: String,
    val provider: Provider,
    val role : Role,
    val isOnboarding: Boolean,
    val isNotification: Boolean,
    val categories: List<String> = emptyList(),
    val ageGroup: String?,
    val weight: String?,
){
    companion object {
        fun from(user: User) = UserResponse(
            email = user.email,
            nickname = user.nickname,
            provider = user.provider,
            role = user.role,
            isOnboarding = user.isOnboarding,
            isNotification = user.isNotification,
            categories = user.categories.map { it.category.name },
            ageGroup = user.ageGroup?.name,
            weight = user.weight?.name
        )
    }
}
