package com.spring.localparking.operatingHour.domain

import jakarta.persistence.*

@Entity
@Table(name = "operating_hour")
class OperatingHour(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToMany(
        mappedBy = "operatingHour",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val timeSlots: MutableList<TimeSlot> = mutableListOf()
) {
    fun addTimeSlot(timeSlot: TimeSlot) {
        timeSlots.add(timeSlot)
        timeSlot.operatingHour = this
    }
}