package com.spring.localparking.parking.dto

import com.spring.localparking.operatingHour.domain.OperatingHour
import com.spring.localparking.operatingHour.domain.TimeSlot
import java.time.DayOfWeek
import java.time.LocalTime

data class OperatingSlot(
    val begin: String,
    val end: String
)

data class GroupedOperatingHoursDto(
    val label: String,
    val slot: OperatingSlot?
)

fun buildGroupedWeek(op: OperatingHour?): List<GroupedOperatingHoursDto> {
    if (op == null || op.timeSlots.isEmpty()) {
        return listOf(
            GroupedOperatingHoursDto("평일", null),
            GroupedOperatingHoursDto("주말", null),
            GroupedOperatingHoursDto("공휴일", null)
        )
    }

    fun format(slot: TimeSlot): OperatingSlot? {
        if (slot.beginTime == slot.endTime) return null

        val begin = "%02d:%02d".format(slot.beginTime?.hour, slot.beginTime?.minute)
        var end = "%02d:%02d".format(slot.endTime?.hour, slot.endTime?.minute)

        if (slot.endTime == LocalTime.MIDNIGHT || (slot.endTime?.hour == 23 && slot.endTime?.minute == 59) || slot.endTime == LocalTime.MAX) {
            end = "24:00"
        }

        return OperatingSlot(begin, end)
    }

    val weekday = op.timeSlots.firstOrNull { it.dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY }
    val weekend = op.timeSlots.firstOrNull { it.dayOfWeek == DayOfWeek.SATURDAY }
    val holiday = op.timeSlots.firstOrNull { it.dayOfWeek == DayOfWeek.SUNDAY }

    return listOf(
        GroupedOperatingHoursDto("평일", weekday?.let(::format)),
        GroupedOperatingHoursDto("주말", weekend?.let(::format)),
        GroupedOperatingHoursDto("공휴일", holiday?.let(::format))
    )
}