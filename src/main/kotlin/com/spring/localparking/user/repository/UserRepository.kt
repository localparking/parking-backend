package com.spring.localparking.user.repository

import com.spring.localparking.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository :JpaRepository<User, Long> {
    fun findByProviderId(providerId: Long): User?
    fun findByAdminId(adminId: String): User?
}