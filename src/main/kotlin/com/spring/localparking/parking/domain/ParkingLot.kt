package com.spring.localparking.parking.domain

import com.spring.localparking.api.dto.ParkingInfo
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
    var tel: String? = null,
    var address: String? = null,
    var lat: Double? = null,
    var lon: Double? = null,
    var isCoalition: Boolean = false,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "fee_policy_id", referencedColumnName = "id")
    var feePolicy: FeePolicy?,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "operating_hour_id", referencedColumnName = "id")
    var operatingHour: OperatingHour?,

    @OneToMany(mappedBy = "parkingLot", cascade = [CascadeType.ALL], orphanRemoval = true)
    val storeParkingLots: MutableSet<StoreParkingLot> = mutableSetOf()
) {
    fun updateInfo(info: ParkingInfo, feePolicy: FeePolicy, operatingHour: OperatingHour) {
        this.name = info.parkingName
        this.address = info.address
        this.parkingType = info.parkingType
        this.capacity = info.capacity?.toIntOrNull()
        this.isRealtime = info.isRealtimeEnabled
        this.isFree = info.isPaid == "N"
        this.tel = info.tel
        this.lat = info.latitude?.toDoubleOrNull()
        this.lon = info.longitude?.toDoubleOrNull()
        this.feePolicy = feePolicy
        this.operatingHour = operatingHour
    }

    companion object {
        fun from(info: ParkingInfo, feePolicy: FeePolicy, operatingHour: OperatingHour): ParkingLot {
            return ParkingLot(
                parkingCode = info.parkingCode,
                name = info.parkingName,
                address = info.address,
                parkingType = info.parkingType,
                capacity = info.capacity?.toIntOrNull(),
                isRealtime = info.isRealtimeEnabled,
                isFree = info.isPaid == "N",
                tel = info.tel,
                lat = info.latitude?.toDoubleOrNull(),
                lon = info.longitude?.toDoubleOrNull(),
                feePolicy = feePolicy,
                operatingHour = operatingHour
            )
        }
    }
}