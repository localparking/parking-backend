package com.spring.localparking.auth.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.auth.dto.OnboardingRequest
import com.spring.localparking.auth.dto.join.RegisterRequest
import com.spring.localparking.auth.dto.join.TermDto
import com.spring.localparking.auth.dto.join.TermsResponse
import com.spring.localparking.global.dto.Age
import com.spring.localparking.global.dto.Provider
import com.spring.localparking.global.dto.Role
import com.spring.localparking.global.dto.Weight
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.user.domain.TermAgreement
import com.spring.localparking.user.domain.UserCategory
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.category.repository.CategoryRepository
import com.spring.localparking.user.repository.TermAgreementRepository
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
    private val termAgreementRepository: TermAgreementRepository
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
        if(user.provider == Provider.NONE) throw CustomException(ErrorCode.UNAUTHORIZED)
        if (user.role == Role.USER) {
            throw CustomException(ErrorCode.ALREADY_REGISTERED)
        }
        val allTerms = termRepository.findAll()
        val agreementMap = request.agreements.associateBy { it.termId }
        val missingTerms = allTerms.filterNot { agreementMap.containsKey(it.id) }
        if (missingTerms.isNotEmpty()) {
            throw CustomException(ErrorCode.MISSING_REQUIRED_TERMS)
        }
        allTerms.forEach { term ->
            val agreement = agreementMap[term.id]
                ?: throw CustomException(ErrorCode.TERM_NOT_FOUND)

            if (term.mandatory && !agreement.agreed) {
                throw CustomException(ErrorCode.REQUIRED_TERM_NOT_AGREED)
            }

            if (term.termType == "MARKETING_OPT_IN") {
                user.updateIsNotification(agreement.agreed)
            }

            val termAgreement = TermAgreement(
                agreed = agreement.agreed,
                agreedAt = if (agreement.agreed) LocalDateTime.now() else null,
                withdrawnAt = if (!agreement.agreed) LocalDateTime.now() else null,
                user = user,
                term = term
            )
            termAgreementRepository.save(termAgreement)
        }
        user.createdAt = LocalDateTime.now()
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