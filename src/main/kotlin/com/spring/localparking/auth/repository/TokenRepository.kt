package com.spring.localparking.auth.repository

import com.spring.localparking.auth.domain.Token
import org.springframework.data.jpa.repository.JpaRepository

interface TokenRepository : JpaRepository<Token, Long>{
    fun findByUserId(userId: Long): Token?
}