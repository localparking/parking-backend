package com.spring.localparking.operatingHour.domain

import java.time.DayOfWeek
import java.time.LocalTime

data class NormalizedSlot(
    val dayOfWeek: DayOfWeek,
    val begin: LocalTime?,
    val end: LocalTime?,
    val overnight: Boolean
)

fun NormalizedSlot.isValid(): Boolean = begin != null && end != null && begin != end
