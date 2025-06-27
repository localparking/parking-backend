package com.spring.localparking.auth.service

import com.spring.localparking.auth.domain.Token
import com.spring.localparking.auth.repository.TokenRepository
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TokenService (
    private val tokenRepository: TokenRepository,
    private val userRepository: UserRepository
){
    @Transactional
    fun saveRefreshToken(userId: Long, refreshToken: String) {
        userRepository.findById(userId)
            ?: throw UserNotFoundException()

        val token = Token(userId, refreshToken)
        tokenRepository.save(token)
    }

    @Transactional
    fun renewRefreshToken(userId: Long, newRefreshToken: String) {
        val token = tokenRepository.findByUserId(userId)
            ?: throw UserNotFoundException()

        token.updateRefreshToken(newRefreshToken)
        tokenRepository.save(token)
    }
}