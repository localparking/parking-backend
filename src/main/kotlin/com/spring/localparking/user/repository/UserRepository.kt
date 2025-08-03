package com.spring.localparking.user.repository

import com.spring.localparking.global.dto.Provider
import com.spring.localparking.global.dto.RequestStatus
import com.spring.localparking.global.dto.Role
import com.spring.localparking.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository :JpaRepository<User, Long> {
    fun findByProviderAndProviderId(provider: Provider, providerId: String): User?
    fun findByAdminId(adminId: String): User?
    fun findByEmail(email: String): User?
    fun findByRoleAndRegistrationStatus(role: Role, status: RequestStatus): List<User>
}