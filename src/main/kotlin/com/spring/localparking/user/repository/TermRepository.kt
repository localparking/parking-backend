package com.spring.localparking.user.repository

import com.spring.localparking.user.domain.Term
import org.springframework.data.jpa.repository.JpaRepository

interface TermRepository: JpaRepository<Term, Long> {
    fun findByTermType(termType: String): Term?
}