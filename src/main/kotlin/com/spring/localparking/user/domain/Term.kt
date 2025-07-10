package com.spring.localparking.user.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table
class Term (
    @Column(nullable = false)
    var termType : String,
    var version: String? = null,
    var title: String? = null,
    var content: String? = null,
    var effectiveDate: LocalDateTime = LocalDateTime.now(),
    var mandatory: Boolean = false,

    @OneToMany(mappedBy = "term")
    var users: MutableList<TermAgreement> = mutableListOf()
){
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}