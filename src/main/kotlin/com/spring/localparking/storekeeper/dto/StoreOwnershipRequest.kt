package com.spring.localparking.storekeeper.dto

import com.spring.localparking.global.dto.RequestStatus
import com.spring.localparking.store.domain.Store
import com.spring.localparking.user.domain.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "store_ownership_request")
class StoreOwnershipRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    val store: Store,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RequestStatus = RequestStatus.PENDING,

    @Column(nullable = false)
    val requestedAt: LocalDateTime = LocalDateTime.now(),

    var processedAt: LocalDateTime? = null
)