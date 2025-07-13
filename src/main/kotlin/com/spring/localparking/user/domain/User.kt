package com.spring.localparking.user.domain

import com.spring.localparking.auth.domain.Token
import com.spring.localparking.global.dto.Age
import com.spring.localparking.global.dto.Provider
import com.spring.localparking.global.dto.Role
import com.spring.localparking.global.dto.Weight
import jakarta.persistence.*

@Entity
@Table(name="user",
    uniqueConstraints = [UniqueConstraint(columnNames = ["provider", "providerId"])]
)
class User protected constructor(
    @Column(unique = true)
    var adminId : String?=null,
    var password : String?=null,
    @Enumerated(EnumType.STRING)
    var provider: Provider,
    var providerId : String? = null,
    var nickname: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @Enumerated(EnumType.STRING)
    var role : Role = Role.GUEST,
    var isOnboarding: Boolean = false,
    var isNotification: Boolean = false,
    @Enumerated(EnumType.STRING)
    var ageGroup: Age? = null,
    @Enumerated(EnumType.STRING)
    var weight: Weight? = null,

    @OneToMany(
        mappedBy = "user",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var tokens: MutableSet<Token> = mutableSetOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var categories: MutableList<UserCategory> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var terms: MutableList<TermAgreement> = mutableListOf()
){
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?= null
        protected set

    companion object {
        fun ofProvider(
            provider   : Provider,
            providerId : String,
            nickname   : String,
            email      : String?
        ) = User(
            provider   = provider,
            providerId = providerId,
            nickname   = nickname,
            email      = email ?: "unknown@placeholder"
        )
    }
    protected constructor() : this(
        provider   = Provider.NONE,
        providerId = "",
        nickname   = "",
        email      = "unknown@placeholder"
    )
    fun updateRole() {
        this.role = Role.USER
    }
    fun updateIsNotification(isNotification: Boolean) {
        this.isNotification = isNotification
    }
}