package com.spring.localparking.store.domain

import com.spring.localparking.auth.dto.storekeeper.StoreManualRequestDto
import com.spring.localparking.global.dto.StoreType
import com.spring.localparking.operatingHour.domain.OperatingHour
import com.spring.localparking.store.domain.location.DoroAddress
import com.spring.localparking.store.domain.location.Location
import com.spring.localparking.storekeeper.domain.StoreParkingBenefit
import com.spring.localparking.user.domain.User
import jakarta.persistence.*

@Entity
@Table(name = "store")
data class Store(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    var businessNumber: String? = null,

    @OneToMany(mappedBy = "store", cascade = [CascadeType.ALL], orphanRemoval = true)
    var categories: MutableSet<StoreCategory> = mutableSetOf(),

    @Column(nullable = false)
    val name: String,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "location_id")
    var location: Location,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "operating_hour_id")
    var operatingHour: OperatingHour? = null,

    var tel: String?=null,

    @Enumerated(EnumType.STRING)
    var storeType: StoreType? = StoreType.GENERAL,

    @OneToMany(mappedBy = "store", cascade = [CascadeType.ALL], orphanRemoval = true)
    val storeParkingLots: MutableSet<StoreParkingLot> = mutableSetOf(),

    @OneToMany(mappedBy = "store", cascade = [CascadeType.ALL], orphanRemoval = true)
    val parkingBenefits: MutableSet<StoreParkingBenefit> = mutableSetOf(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", unique = true)
    var owner: User? = null,

    @OneToMany(mappedBy = "store", cascade = [CascadeType.ALL], orphanRemoval = true)
    val products: MutableList<Product> = mutableListOf()

    ) {
    companion object {
        fun of(storeInfo: StoreManualRequestDto, businessNumber: String, owner: User): Store {
            val storeAddress = storeInfo.storeAddress

            val fullAddress = with(storeAddress) {
                "$sido $sigungu $doroName $buildingNo"
            }.trim()

            val doroAddress = DoroAddress(
                sido = storeAddress.sido!!,
                sigungu = storeAddress.sigungu!!,
                doroName = storeAddress.doroName!!,
                buildingNo = storeAddress.buildingNo!!,
                fullAddress = fullAddress
            )

            val location = Location(
                id = 0L,
                doroAddress = doroAddress,
                jibeonAddress = null,
                lat = storeAddress.lat!!,
                lon = storeAddress.lon!!
            )

            return Store(
                name = storeInfo.storeName!!,
                businessNumber = businessNumber,
                location = location,
                owner = owner,
                tel = storeInfo.tel
            )
        }
    }
}