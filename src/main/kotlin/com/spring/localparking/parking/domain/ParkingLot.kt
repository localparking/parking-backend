package com.spring.localparking.parking.domain

import com.spring.localparking.api.dto.SeoulApiDto // [추가됨]
import com.spring.localparking.store.domain.StoreParkingLot
import jakarta.persistence.*

@Entity
@Table(name = "parking_lot")
class ParkingLot (
    @Id
    @Column(name = "parking_code")
    val parkingCode: String,
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
    var feePolicy: FeePolicy?,

    @OneToMany(mappedBy = "parkingLot", cascade = [CascadeType.ALL], orphanRemoval = true)
    val operatingHours: List<OperatingHour> = mutableListOf(),

    @OneToMany(mappedBy = "parkingLot", cascade = [CascadeType.ALL], orphanRemoval = true)
    val storeParkingLots: MutableSet<StoreParkingLot> = mutableSetOf()
) {
    fun updateInfo(info: SeoulApiDto.ParkingInfo, feePolicy: FeePolicy) {
        this.name = info.parkingName
        this.address = info.address
        this.parkingType = info.parkingType
        this.capacity = info.capacity?.toIntOrNull()
        this.isRealtime = info.isRealtimeEnabled == "Y"
        this.isFree = info.isPaid == "N"
        this.lat = info.latitude?.toDoubleOrNull()
        this.lon = info.longitude?.toDoubleOrNull()
        this.feePolicy = feePolicy
    }

    companion object {
        fun from(info: SeoulApiDto.ParkingInfo, feePolicy: FeePolicy): ParkingLot {
            return ParkingLot(
                parkingCode = info.parkingCode,
                name = info.parkingName,
                address = info.address,
                parkingType = info.parkingType,
                capacity = info.capacity?.toIntOrNull(),
                isRealtime = info.isRealtimeEnabled == "Y",
                isFree = info.isPaid == "N",
                lat = info.latitude?.toDoubleOrNull(),
                lon = info.longitude?.toDoubleOrNull(),
                feePolicy = feePolicy
            )
        }
    }
}