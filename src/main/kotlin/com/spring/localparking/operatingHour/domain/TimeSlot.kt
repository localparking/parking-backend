package com.spring.localparking.operatingHour.domain

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
@Table(name = "time_slot")
class TimeSlot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val dayOfWeek: DayOfWeek,

    @Column(nullable = false)
    val beginTime: LocalTime,

    @Column(nullable = false)
    val endTime: LocalTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operating_hour_id")
    var operatingHour: OperatingHour? = null
) {
    fun contains(time: LocalTime): Boolean {
        if (endTime.isBefore(beginTime)) {
            return !time.isBefore(beginTime) || !time.isAfter(endTime)
        }
        return !time.isBefore(beginTime) && time.isBefore(endTime)
    }
}