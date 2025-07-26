package com.spring.localparking.search.domain

import com.spring.localparking.user.domain.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class RecentSearch(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(nullable = false)
    val query: String,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}