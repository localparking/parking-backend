package com.spring.localparking.auth.domain

import jakarta.persistence.*

@Entity
@Table(name = "token")
class Token (
    @Column(nullable = false)
    var userId: Long,
    @Column(nullable = false)
    var refreshToken: String
){
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    fun updateRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
    }

    protected  constructor() : this(
        userId = 0L,
        refreshToken = "")
}