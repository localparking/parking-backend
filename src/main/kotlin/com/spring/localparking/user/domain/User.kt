package com.spring.localparking.user.domain

import com.spring.localparking.global.Provider
import com.spring.localparking.global.Role
import jakarta.persistence.*

@Entity
@Table(name="user")
class User (
    @Column(unique = true)
    var adminId : String?=null,
    var password : String?=null,
    @Enumerated(EnumType.STRING)
    var provider: Provider,
    var providerId : Long,
    var nickname: String,
    @Enumerated(EnumType.STRING)
    var role : Role
){
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?= null

    companion object {
        fun ofProvider(provider: Provider, providerId: Long, nickname: String): User =
            User(
                provider = provider,
                providerId = providerId,
                nickname = nickname,
                role = Role.USER
            )
    }
    constructor() : this(null, null, Provider.KAKAO, 0L, "", Role.USER)
}