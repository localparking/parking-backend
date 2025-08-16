package com.spring.localparking.user.repository

import com.spring.localparking.user.domain.Term
import com.spring.localparking.user.domain.TermAgreement
import com.spring.localparking.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface TermAgreementRepository: JpaRepository<TermAgreement, Long> {
    fun findByUserAndTerm(user: User, term: Term): TermAgreement?
}