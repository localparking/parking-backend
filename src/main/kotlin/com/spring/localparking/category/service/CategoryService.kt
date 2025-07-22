package com.spring.localparking.category.service

import com.spring.localparking.category.repository.CategoryRepository
import com.spring.localparking.category.dto.CategoryDto
import com.spring.localparking.category.dto.CategoryResponse
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

    fun getAllCategories(): CategoryResponse {
        val categories = categoryRepository.findAllBy()
        return CategoryResponse(categories.map {
            CategoryDto(
                categoryId = it.id!!,
                categoryName = it.name,
                parentId = it.parent?.id
            )
        })
    }
}