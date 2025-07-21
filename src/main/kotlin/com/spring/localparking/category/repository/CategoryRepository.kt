package com.spring.localparking.category.repository

import com.spring.localparking.category.domain.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository: JpaRepository<Category, Long> {
    fun findAllByParentIsNull(): List<Category>
    fun findAllByParentId(parentId: Long): List<Category>
    fun findAllBy(): List<Category>
}