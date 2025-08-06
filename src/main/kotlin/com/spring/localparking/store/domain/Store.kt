package com.spring.localparking.store.domain

import com.spring.localparking.auth.dto.storekeeper.StorekeeperRegisterRequest
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

    val tel: String?=null,

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

    ){
    companion object {
        fun of(request: StorekeeperRegisterRequest, owner: User): Store {
            val fullAddress = with(request.storeAddress) {
                "$sido $sigungu $doroName $buildingNo"
            }.trim()

            val doroAddress = DoroAddress(
                sido = request.storeAddress.sido,
                sigungu = request.storeAddress.sigungu,
                doroName = request.storeAddress.doroName,
                buildingNo = request.storeAddress.buildingNo,
                fullAddress = fullAddress
            )

            val location = Location(
                doroAddress = doroAddress,
                jibeonAddress = null,
                lat = request.storeAddress.lat,
                lon = request.storeAddress.lon,
                id = 0L
            )

            return Store(
                name = request.storeName,
                businessNumber = request.businessNumber,
                location = location,
                owner = owner
            )
        }
    }
}