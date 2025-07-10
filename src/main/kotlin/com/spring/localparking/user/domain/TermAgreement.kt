package com.spring.localparking.user.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "term_agreement")
class TermAgreement (
    var agreed: Boolean,
    var agreedAt: LocalDateTime ?= null,
    var withdrawnAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    var term: Term

){
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}