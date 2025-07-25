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

    @Column(nullable = true)
    val beginTime: LocalTime? = null,

    @Column(nullable = true)
    val endTime: LocalTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operating_hour_id")
    var operatingHour: OperatingHour? = null
) {
    fun isValid(): Boolean = beginTime != null && endTime != null && beginTime != endTime

    fun isOvernight(): Boolean = isValid() && endTime!!.isBefore(beginTime!!)
    fun containsOrNull(time: LocalTime): Boolean? {
        if (!isValid()) return null
        val start = beginTime!!
        val end = endTime!!
        return if (isOvernight()) {
            !time.isBefore(start) || time.isBefore(end)
        } else {
            !time.isBefore(start) && time.isBefore(end)
        }
    }
}

