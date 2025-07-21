package com.spring.localparking.operatingHour.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class NormalizedSlot(
    val dayOfWeek: DayOfWeek,
    val begin: LocalTime,
    val end: LocalTime,
    val overnight: Boolean
) {
    fun contains(dateTime: LocalDateTime): Boolean {
        val t = dateTime.toLocalTime()
        val dow = dateTime.dayOfWeek
        return if (!overnight) {
            (dow == dayOfWeek) && !t.isBefore(begin) && t.isBefore(end)
        } else {
            if (dow == dayOfWeek && !t.isBefore(begin)) true
            else if (dow == dayOfWeek.plus(1) && t.isBefore(end)) true
            else false
        }
    }
    fun effectiveEndDateTime(baseDate: LocalDate): LocalDateTime {
        val date = if (!overnight) baseDate else baseDate.plusDays(1)
        return LocalDateTime.of(date, end)
    }
}
