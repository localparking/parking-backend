package com.spring.localparking.category.service

import com.spring.localparking.category.repository.CategoryRepository
import com.spring.localparking.user.dto.CategoryDto
import com.spring.localparking.user.dto.CategoryResponse
import org.springframework.stereotype.Service

@Service
class CategoryService (
    private val categoryRepository: CategoryRepository
){
    fun getParentCategories(): CategoryResponse {
        val parentCategories = categoryRepository.findAllByParentIsNull()
        return CategoryResponse(parentCategories.map {
            CategoryDto(
                categoryId = it.id!!,
                categoryName = it.name
            )
        })
    }

    fun getChildCategories(parentId: Long): CategoryResponse {
        val childCategories = categoryRepository.findAllByParentId(parentId)
        return CategoryResponse(childCategories.map {
            CategoryDto(
                categoryId = it.id!!,
                categoryName = it.name
            )
        })
    }
}