package com.spring.localparking.parking.domain

import com.spring.localparking.global.dto.DayOfWeek
import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "operating_hour")
class OperatingHour(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    val dayOfWeek: DayOfWeek,

    @Column(name = "open_time")
    val openTime: LocalTime?,

    @Column(name = "close_time")
    val closeTime: LocalTime?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", referencedColumnName = "parking_code")
    val parkingLot: ParkingLot
)
