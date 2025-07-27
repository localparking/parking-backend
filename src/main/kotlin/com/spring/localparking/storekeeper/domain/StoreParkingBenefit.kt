package com.spring.localparking.storekeeper.domain

import com.spring.localparking.store.domain.Store
import jakarta.persistence.*

@Entity
@Table(name = "store_parking_benefit")
class StoreParkingBenefit (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    var store: Store,

    @Column(nullable = false)
    var purchaseAmount: Int,

    @Column(nullable = false)
    var discountMin: Int
)
