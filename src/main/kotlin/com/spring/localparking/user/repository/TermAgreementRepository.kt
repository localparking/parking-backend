package com.spring.localparking.user.repository

import com.spring.localparking.user.domain.TermAgreement
import org.springframework.data.jpa.repository.JpaRepository

interface TermAgreementRepository: JpaRepository<TermAgreement, Long> {
}