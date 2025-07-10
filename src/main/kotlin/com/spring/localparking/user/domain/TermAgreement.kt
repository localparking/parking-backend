package com.spring.localparking.user.domain

import jakarta.persistence.*

@Entity
@Table(name = "term_agreement")
class TermAgreement (
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