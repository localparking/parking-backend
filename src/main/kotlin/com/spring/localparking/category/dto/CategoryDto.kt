package com.spring.localparking.category.dto

data class CategoryDto(
    val categoryId: Long,
    val categoryName: String?,
    val parentId: Long? = null
)