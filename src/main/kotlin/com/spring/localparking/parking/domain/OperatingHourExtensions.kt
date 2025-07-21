package com.spring.localparking.operatingHour.domain

import java.time.*

/**
 * 오늘(day) 24시간 여부 (00:00 시작 ~ 23:59 혹은 LocalTime.MAX)
 */
fun OperatingHour.is24Hours(day: DayOfWeek): Boolean =
    timeSlots.any {
        it.dayOfWeek == day &&
                it.beginTime == LocalTime.MIDNIGHT &&
                (it.endTime == LocalTime.MAX || (it.endTime.hour == 23 && it.endTime.minute == 59))
    }

/**
 * 특정 시각(now) 기준 영업 중 여부 (overnight 포함).
 * timeSlots 비어 있으면 null.
 */
fun OperatingHour.isOpened(now: LocalDateTime): Boolean? {
    if (timeSlots.isEmpty()) return null
    val day = now.dayOfWeek
    val t = now.toLocalTime()

    if (is24Hours(day)) return true

    val todaySlots = timeSlots.filter { it.dayOfWeek == day }
    val yesterday = day.minus(1)
    val overnightFromYesterday = timeSlots.filter {
        it.dayOfWeek == yesterday && it.isOvernight()
    }

    if (todaySlots.any { it.contains(t) }) return true
    if (overnightFromYesterday.any { t.isBefore(it.endTime) }) return true

    return false
}

/**
 * NormalizedSlot: Presentation용으로 begin<=end 형태로 ‘당일 기준’ 표준화.
 * Overnight 은 그대로 표시(overnight=true) 하되 end 는 원래 endTime.
 * (추후 Presenter에서 필요)
 */
fun OperatingHour.normalizedSlots(): List<NormalizedSlot> =
    timeSlots.map {
        NormalizedSlot(
            dayOfWeek = it.dayOfWeek,
            begin = it.beginTime,
            end = it.endTime,
            overnight = it.isOvernight()
        )
    }

/**
 * 현재 열림 여부 + '오늘 마감 시간' (여러 슬롯/겹침 고려).
 *
 * - return Pair<isOpen, closingTime>
 * - isOpen == null : 슬롯 자체가 없음
 * - closingTime: 현재 열려 있으면 (해당 겹치는/병합된 가장 늦은 end) 의 LocalTime
 */
fun OperatingHour.openStatus(now: LocalDateTime = LocalDateTime.now()): Pair<Boolean?, LocalTime?> {
    if (timeSlots.isEmpty()) return null to null

    val day = now.dayOfWeek
    val localDate = now.toLocalDate()
    val time = now.toLocalTime()

    // 24시간
    if (is24Hours(day)) {
        return true to LocalTime.MAX
    }

    // 오늘 슬롯 + 어제 overnight 슬롯
    val todaySlots = timeSlots.filter { it.dayOfWeek == day }
    val ySlots = timeSlots.filter { it.dayOfWeek == day.minus(1) && it.isOvernight() }

    val activeSlots = mutableListOf<TimeSlot>()

    todaySlots.forEach { if (it.contains(time)) activeSlots += it }
    ySlots.forEach { if (time.isBefore(it.endTime)) activeSlots += it }

    if (activeSlots.isEmpty()) {
        return false to null
    }

    val nowDate = now.toLocalDate()
    val latestEnd = activeSlots.maxByOrNull { slot ->
        if (slot.isOvernight()) nowDate.plusDays(1).atTime(slot.endTime)
        else nowDate.atTime(slot.endTime)
    }!!.let {
        if (it.isOvernight())
            activeSlots.first { s -> s.isOvernight() }.endTime  // 로컬타임만 반환
        else it.endTime
    }


    return true to latestEnd
}
