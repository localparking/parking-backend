package com.spring.localparking.store.domain

import com.spring.localparking.parking.domain.ParkingLot
import jakarta.persistence.*

@Entity
@Table(name = "store_parking_lot")
class StoreParkingLot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    val store: Store,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id")
    @JoinColumn(name = "parking_lot_code", referencedColumnName = "parking_code")
    val parkingLot: ParkingLot
)