package com.spring.localparking.operatingHour.domain

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "operating_hour")
class OperatingHour(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToMany(mappedBy = "operatingHour", cascade = [CascadeType.ALL], orphanRemoval = true)
    val timeSlots: MutableList<TimeSlot> = mutableListOf()
) {
    // 현재 시간에 운영 중인지 확인하는 로직
    fun isOpenNow(): Boolean? {
        if (timeSlots.isEmpty()) {
            return null
        }
        return isOpen(LocalDate.now().dayOfWeek, LocalTime.now())
    }

    // 특정 요일, 특정 시간에 운영 중인지 확인하는 로직
    fun isOpen(dayOfWeek: DayOfWeek, time: LocalTime): Boolean? {
        if (is24Hours(dayOfWeek)) {
            return true
        }
        if(timeSlots.isEmpty()) {
            return null
        }
        return timeSlots.any { it.dayOfWeek == dayOfWeek && it.contains(time) }
    }

    // 24시간 운영하는지 확인
    fun is24Hours(dayOfWeek: DayOfWeek): Boolean {
        return timeSlots.any {
            it.dayOfWeek == dayOfWeek &&
                    it.beginTime == LocalTime.MIN &&
                    it.endTime == LocalTime.MAX
        }
    }

    fun addTimeSlot(timeSlot: TimeSlot) {
        timeSlots.add(timeSlot)
        timeSlot.operatingHour = this
    }
}