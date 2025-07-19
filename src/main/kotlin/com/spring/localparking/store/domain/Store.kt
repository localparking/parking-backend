package com.spring.localparking.store.domain

import com.spring.localparking.operatingHour.domain.OperatingHour
import jakarta.persistence.*

@Entity
@Table(name = "store")
data class Store(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToMany(mappedBy = "store", cascade = [CascadeType.ALL], orphanRemoval = true)
    var categories: MutableList<StoreCategory> = mutableListOf(),

    @Column(nullable = false)
    val name: String,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "location_id")
    var location: Location,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "operating_hour_id")
    var operatingHour: OperatingHour? = null,

    val tel: String?,

    @Column(name = "is_coalition", nullable = false)
    val isCoalition: Boolean,

    @OneToMany(mappedBy = "store", cascade = [CascadeType.ALL], orphanRemoval = true)
    val storeParkingLots: MutableSet<StoreParkingLot> = mutableSetOf()
)