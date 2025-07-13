package com.spring.localparking.store.domain

import com.spring.localparking.global.dto.DayOfWeek
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

    @Column(nullable = false)
    val address: String,

    @Column(nullable = false)
    val lat: Double,

    @Column(nullable = false)
    val lon: Double,

    val tel: String?,

    @Column(name = "is_pet_friendly")
    val isPetFriendly: Boolean?,

    @Column(name = "website_url")
    val websiteUrl: String?,

    @Column(name = "operating_info")
    val operatingInfo: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "closed_days")
    val closedDays: DayOfWeek?,

    @Column(name = "has_nursing_room")
    val hasNursingRoom: Boolean?,

    @Column(name = "has_stroller_rental")
    val hasStrollerRental: Boolean?,

    @Column(name = "has_kids_zone")
    val hasKidsZone: Boolean?,

    @Column(name = "has_info")
    val hasInfo: Boolean?,

    @Column(name = "is_coalition", nullable = false)
    val isCoalition: Boolean,

    @OneToMany(mappedBy = "store", cascade = [CascadeType.ALL], orphanRemoval = true)
    val storeParkingLots: MutableSet<StoreParkingLot> = mutableSetOf()
)