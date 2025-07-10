package com.spring.localparking.auth.domain

import com.spring.localparking.user.domain.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "token")
class Token (
    @Column(nullable = false)
    var refreshToken: String,
    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User
){
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    fun updateRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
        this.createdAt = LocalDateTime.now()
    }
}