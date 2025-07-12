package com.spring.localparking.parking.domain

import com.spring.localparking.store.domain.StoreParkingLot
import jakarta.persistence.*

@Entity
@Table(name = "parking_lot")
class ParkingLot (
    @Id
    @Column(name = "parking_code")
    val parkingCode: Long,
    var name: String,
    var parkingType: String?= null,
    var capacity: Int? = null,
    var isRealtime: Boolean = false,
    var isFree: Boolean = false,
    var address: String? = null,
    var lat: Double? = null,
    var lon: Double? = null,
    var isCoalition: Boolean = false,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "fee_policy_id", referencedColumnName = "id")
    val feePolicy: FeePolicy?,

    @OneToMany(mappedBy = "parkingLot", cascade = [CascadeType.ALL], orphanRemoval = true)
    val operatingHours: List<OperatingHour> = mutableListOf(),

    @OneToMany(mappedBy = "parkingLot", cascade = [CascadeType.ALL], orphanRemoval = true)
    val storeParkingLots: MutableSet<StoreParkingLot> = mutableSetOf()
)