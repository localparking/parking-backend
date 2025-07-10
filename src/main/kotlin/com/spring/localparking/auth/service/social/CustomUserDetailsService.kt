package com.spring.localparking.auth.service.social

import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService (
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(adminId: String): UserDetails {
        val user = userRepository.findByAdminId(adminId)
            ?: throw UserNotFoundException()
        return CustomPrincipal(user, mutableMapOf())
    }
}