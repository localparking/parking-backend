package com.spring.localparking.user.service

import com.spring.localparking.auth.service.TokenService
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository,
    private val tokenService: TokenService
){
    @Transactional
    fun withdrawUser(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }

        user.withdraw()

        tokenService.deleteRefreshToken(userId)
    }
}