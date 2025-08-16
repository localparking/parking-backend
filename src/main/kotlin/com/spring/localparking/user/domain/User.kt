package com.spring.localparking.user.domain

import com.spring.localparking.auth.domain.Token
import com.spring.localparking.auth.dto.storekeeper.StorekeeperRegisterRequest
import com.spring.localparking.global.dto.*
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name="users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["provider", "providerId"])]
)
class User protected constructor(
    @Column(unique = true)
    var adminId : String?=null,
    var password : String?=null,
    @Enumerated(EnumType.STRING)
    var provider: Provider = Provider.NONE,
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
    var createdAt: LocalDateTime? = null,
    var withdrawnAt: LocalDateTime? = null,
    @Enumerated(EnumType.STRING)
    var registrationStatus: RequestStatus = RequestStatus.PENDING,

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var userProfile: UserProfile? = null,

    @OneToMany(
        mappedBy = "user",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var tokens: MutableSet<Token> = mutableSetOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var categories: MutableList<UserCategory> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var terms: MutableList<TermAgreement> = mutableListOf(),

    ){
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?= null
        protected set

    fun associateProfile() {
        this.userProfile = UserProfile(userId = this.id!!, user = this)
    }

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

        fun ofStorekeeper(request: StorekeeperRegisterRequest, encodedPassword: String): User {
            return User(
                adminId = request.adminId,
                password = encodedPassword,
                provider = Provider.NONE,
                nickname = request.adminId,
                email = request.email,
                role = Role.STOREKEEPER,
                registrationStatus = RequestStatus.PENDING
            )
        }
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
    fun withdraw() {
        this.role = Role.WITHDRAWN
        this.withdrawnAt = LocalDateTime.now()
    }
}