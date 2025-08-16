package com.spring.localparking.auth.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.auth.dto.join.RegisterRequest
import com.spring.localparking.auth.dto.join.TermDto
import com.spring.localparking.auth.dto.join.TermsResponse
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.user.domain.TermAgreement
import com.spring.localparking.user.domain.User
import com.spring.localparking.user.repository.TermAgreementRepository
import com.spring.localparking.user.repository.TermRepository
import org.springframework.stereotype.Service
import jakarta.transaction.Transactional
import java.time.LocalDateTime

@Service
class TermService(
    private val termRepository: TermRepository,
    private val termAgreementRepository: TermAgreementRepository
) {
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
    fun processAgreements(user: com.spring.localparking.user.domain.User, request: RegisterRequest) {
        val allTerms = termRepository.findAll()
        val agreementMap = request.agreements.associateBy { it.termId }

        val missingTerms = allTerms.filterNot { agreementMap.containsKey(it.id) }
        if (missingTerms.isNotEmpty()) {
            throw CustomException(ErrorCode.MISSING_REQUIRED_TERMS)
        }

        allTerms.forEach { term ->
            val agreement = agreementMap[term.id] ?: throw CustomException(ErrorCode.TERM_NOT_FOUND)

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
    }
    @Transactional
    fun updateMarketingAgreement(user: User, isAgreed: Boolean) {
        val marketingTerm = termRepository.findByTermType("MARKETING_OPT_IN")
            ?: throw CustomException(ErrorCode.TERM_NOT_FOUND)
        var agreement = termAgreementRepository.findByUserAndTerm(user, marketingTerm)
        if (agreement == null) {
            agreement = TermAgreement(
                user = user,
                term = marketingTerm,
                agreed = isAgreed,
                agreedAt = if (isAgreed) LocalDateTime.now() else null,
                withdrawnAt = if (!isAgreed) LocalDateTime.now() else null
            )
        } else {
            agreement.agreed = isAgreed
            if (isAgreed) {
                agreement.agreedAt = LocalDateTime.now()
                agreement.withdrawnAt = null
            } else {
                agreement.withdrawnAt = LocalDateTime.now()
            }
        }
        termAgreementRepository.save(agreement)
    }
}