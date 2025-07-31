package com.spring.localparking.category.service


import com.spring.localparking.category.domain.Category
import com.spring.localparking.category.repository.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryResolveService(
    private val categoryRepository: CategoryRepository
) {

    @Transactional(readOnly = true)
    fun resolveAll(categoryIds: List<Long>?): List<Long>? {
        if (categoryIds.isNullOrEmpty()) {
            return null
        }
        return categoryIds
            .mapNotNull { resolveId(it) }
            .flatten()
            .distinct()
    }

    @Transactional(readOnly = true)
    fun resolveId(categoryId: Long?): List<Long>? {
        if (categoryId == null) return null
        val category: Category = categoryRepository.findById(categoryId)
            .orElse(null) ?: return null

        return if (category.parent == null) {
            val children = categoryRepository.findAllByParentId(category.id)
            val childIds = children.map { it.id }
            listOf(category.id) + childIds
        } else {
            listOf(category.id)
        }
    }
    @Transactional(readOnly = true)
    fun resolveCategoryNameToQuery(categoryName: String): String {
        val allCategories = categoryRepository.findAllBy()
        val category = allCategories.find { it.name == categoryName } ?: return categoryName

        if (category.parent == null) {
            val children = allCategories.filter { it.parent?.id == category.id }
            if (children.isNotEmpty()) {
                val allNames = listOf(category.name) + children.map { it.name }
                return allNames.joinToString(" OR ")
            }
        }

        return categoryName
    }
}
