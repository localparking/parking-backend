package com.spring.localparking.user.repository

import com.spring.localparking.user.domain.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository: JpaRepository<Category, Long> {
    fun findAllByParentIsNull(): List<Category>
}