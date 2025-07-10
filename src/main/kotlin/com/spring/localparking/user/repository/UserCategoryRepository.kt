package com.spring.localparking.user.repository

import com.spring.localparking.user.domain.UserCategory
import com.spring.localparking.user.domain.UserCategoryId
import org.springframework.data.jpa.repository.JpaRepository

interface UserCategoryRepository : JpaRepository<UserCategory, UserCategoryId> {
    fun findByUserId(userId: Long): List<UserCategory>
}
