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
    fun resolveIds(categoryId: Long?): List<Long>? {
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
}
