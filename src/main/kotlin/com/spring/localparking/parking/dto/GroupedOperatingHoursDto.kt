package com.spring.localparking.operatingHour.dto

import com.spring.localparking.operatingHour.domain.OperatingHour
import com.spring.localparking.operatingHour.domain.TimeSlot
import java.time.DayOfWeek

data class GroupedOperatingHoursDto(
    val label: String,
    val timeRanges: List<String>
)

fun buildGroupedWeek(op: OperatingHour?): List<GroupedOperatingHoursDto> {
    if (op == null || op.timeSlots.isEmpty()) {
        return listOf(
            GroupedOperatingHoursDto("평일", emptyList()),
            GroupedOperatingHoursDto("주말", emptyList()),
            GroupedOperatingHoursDto("공휴일", emptyList())
        )
    }

    fun format(slot: TimeSlot): String =
        "%02d:%02d~%02d:%02d".format(
            slot.beginTime.hour, slot.beginTime.minute,
            slot.endTime.hour, slot.endTime.minute
        )

    val weekday = op.timeSlots.filter { it.dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY }
    val weekend = op.timeSlots.filter { it.dayOfWeek == DayOfWeek.SATURDAY }
    val holiday = op.timeSlots.filter { it.dayOfWeek == DayOfWeek.SUNDAY }

    fun toRanges(list: List<TimeSlot>) =
        list.sortedBy { it.beginTime }.map(::format).distinct()

    return listOf(
        GroupedOperatingHoursDto("평일", toRanges(weekday)),
        GroupedOperatingHoursDto("주말", toRanges(weekend)),
        GroupedOperatingHoursDto("공휴일", toRanges(holiday))
    )
}
