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
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        val existingToken = tokenRepository.findByUserId(userId)
        if (existingToken != null) {
            existingToken.updateRefreshToken(refreshToken)
            tokenRepository.save(existingToken)
        } else {
            userRepository.findById(userId)
            val newToken = Token(
                refreshToken = refreshToken,
                user = user
            )
            tokenRepository.save(newToken)
        }
    }

    @Transactional
    fun renewRefreshToken(userId: Long, newRefreshToken: String) {
        val token = tokenRepository.findByUserId(userId)
            ?: throw UserNotFoundException()

        token.updateRefreshToken(newRefreshToken)
        tokenRepository.save(token)
    }

    @Transactional
    fun deleteRefreshToken(userId: Long) {
        tokenRepository.findByUserId(userId)?.let {
            tokenRepository.deleteByUserId(userId)
        }
    }
}