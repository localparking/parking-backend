package com.spring.localparking.parking.domain

import com.spring.localparking.operatingHour.domain.NormalizedSlot
import com.spring.localparking.operatingHour.domain.OperatingHour
import com.spring.localparking.operatingHour.domain.TimeSlot
import java.time.*

private fun TimeSlot.isValidSlot() = isValid()

/**
 * 오늘(day) 24시간 여부.
 * - 유효 슬롯이 하나도 없으면 null
 * - 00:00 시작 && 23:59 또는 LocalTime.MAX 종료인 유효 슬롯이 있으면 true
 *
 * - 아니면 false
 */
fun OperatingHour.is24Hours(day: DayOfWeek): Boolean? {
    val todayValid = timeSlots.filter { it.dayOfWeek == day && it.isValidSlot() }
    if (todayValid.isEmpty()) return null

    return todayValid.any { slot ->
        slot.beginTime == LocalTime.MIDNIGHT &&
                (
                        slot.endTime == LocalTime.MAX ||
                                (slot.endTime!!.hour == 23 && slot.endTime!!.minute == 59)
                        )
    }
}

/**
 * 특정 시각(now) 기준 영업 중 여부 (overnight 포함).
 * - timeSlots 비어 있으면 null
 * - 유효 슬롯이 하나도 없으면 null
 * - 그 외 true/false
 */
fun OperatingHour.isOpened(now: LocalDateTime): Boolean? {
    if (timeSlots.isEmpty()) return null

    val validSlots = timeSlots.filter { it.isValidSlot() }
    if (validSlots.isEmpty()) return null

    val day = now.dayOfWeek
    val time = now.toLocalTime()

    val is24 = is24Hours(day)
    if (is24 == true) return true

    val todaySlots = validSlots.filter { it.dayOfWeek == day }
    val yesterday = day.minus(1)
    val overnightFromYesterday = validSlots.filter { it.dayOfWeek == yesterday && it.isOvernight() }

    if (todaySlots.any { it.containsOrNull(time) == true }) return true
    if (overnightFromYesterday.any { time.isBefore(it.endTime!!) }) return true

    return false
}

fun OperatingHour.normalizedSlots(): List<NormalizedSlot> =
    timeSlots.map { slot ->
        NormalizedSlot(
            dayOfWeek = slot.dayOfWeek,
            begin = slot.beginTime,
            end = slot.endTime,
            overnight = slot.isOvernight()
        )
    }

/**
 * 현재 열림 여부 + 오늘 마감 시간
 * - isOpen == null : 슬롯이 없거나 전부 invalid
 * - closingTime: 현재 열려 있으면 가장 늦은 end(LocalTime)
 */
fun OperatingHour.openStatus(now: LocalDateTime = LocalDateTime.now()): Pair<Boolean?, LocalTime?> {
    if (timeSlots.isEmpty()) return null to null

    val validSlots = timeSlots.filter { it.isValidSlot() }
    if (validSlots.isEmpty()) return null to null

    val day = now.dayOfWeek
    val time = now.toLocalTime()
    val nowDate = now.toLocalDate()

    val is24 = is24Hours(day)
    if (is24 == true) return true to LocalTime.MAX

    val todaySlots = validSlots.filter { it.dayOfWeek == day }
    val yesterday = day.minus(1)
    val ySlots = validSlots.filter { it.dayOfWeek == yesterday && it.isOvernight() }

    val activeSlots = mutableListOf<TimeSlot>()
    todaySlots.forEach { if (it.containsOrNull(time) == true) activeSlots += it }
    ySlots.forEach { if (time.isBefore(it.endTime!!)) activeSlots += it }

    if (activeSlots.isEmpty()) {
        val hadAnyValidToday = todaySlots.isNotEmpty()
        return (if (hadAnyValidToday) false else null) to null
    }

    val latestEndSlot = activeSlots.maxByOrNull { slot ->
        val end = slot.endTime!!
        if (slot.isOvernight()) nowDate.plusDays(1).atTime(end) else nowDate.atTime(end)
    }!!

    val closingTime = latestEndSlot.endTime
    return true to closingTime
}