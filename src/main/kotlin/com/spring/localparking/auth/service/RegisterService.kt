package com.spring.localparking.auth.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.auth.dto.OnboardingRequest
import com.spring.localparking.auth.dto.join.RegisterRequest
import com.spring.localparking.auth.dto.join.TermDto
import com.spring.localparking.auth.dto.join.TermsResponse
import com.spring.localparking.global.dto.Age
import com.spring.localparking.global.dto.Role
import com.spring.localparking.global.dto.Weight
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.user.domain.UserCategory
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.category.repository.CategoryRepository
import com.spring.localparking.user.repository.TermRepository
import com.spring.localparking.user.repository.UserCategoryRepository
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
open class RegisterService (
    private val termRepository: TermRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val userCategoryRepository: UserCategoryRepository,
    private val termService: TermService
){
    fun getTerms(): TermsResponse {
        val terms = termRepository.findAll()
        return TermsResponse(terms.map {
            TermDto(
                termId = it.id!!,
                version = it.version,
                title = it.title,
                content = it.content,
                mandatory = it.mandatory,
                effectiveDate = it.effectiveDate
            )
        })
    }
    @Transactional
    fun registerAgreements(userId: Long, request: RegisterRequest) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        if (user.role == Role.USER) {
            throw CustomException(ErrorCode.ALREADY_REGISTERED)
        }
        termService.processAgreements(user, request)
        user.updateRole()
    }


    @Transactional
    fun completeOnboarding(userId: Long, request: OnboardingRequest) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        if (user.isOnboarding) { throw CustomException(ErrorCode.ALREADY_ONBOARDED) }

        user.ageGroup = request.ageGroup?.let {
            runCatching { Age.valueOf(it) }.getOrElse {
                throw CustomException(ErrorCode.INVALID_AGE_GROUP)
            }
        }
        user.weight = request.weight?.let {
            runCatching { Weight.valueOf(it) }.getOrElse {
                throw CustomException(ErrorCode.INVALID_WEIGHT)
            }
        }

        updatedCategories(userId, request.categoryIds?.toSet())
        user.isOnboarding = true

    }
    @Transactional
    fun updatedCategories(userId: Long, newIds: Set<Long>?) {
        if (newIds == null) return
        if (newIds.isEmpty()) {
            userCategoryRepository.deleteByUserId(userId)
            return
        }
        val categories = categoryRepository.findAllById(newIds)
        if (categories.size != newIds.size) {
            throw CustomException(ErrorCode.CATEGORY_NOT_FOUND)
        }
        categories.firstOrNull { it.parent != null }?.let {
            throw CustomException(ErrorCode.INVALID_TOP_CATEGORY)
        }
        val userRef = userRepository.getReferenceById(userId)
        userCategoryRepository.deleteByUserId(userId)
        val entities = categories.map { cat ->
            UserCategory(user = userRef, category = cat)
        }
        userCategoryRepository.saveAll(entities)
    }
}