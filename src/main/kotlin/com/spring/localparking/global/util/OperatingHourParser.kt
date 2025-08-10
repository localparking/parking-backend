package com.spring.localparking.global.util

import com.spring.localparking.api.dto.ApiConstants
import com.spring.localparking.operatingHour.DailyOperatingDto
import com.spring.localparking.operatingHour.domain.OperatingHour
import com.spring.localparking.operatingHour.domain.TimeSlot
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object OperatingHourParser {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    fun parse(operatingHourDtos: List<DailyOperatingDto>?): OperatingHour? {
        if (operatingHourDtos.isNullOrEmpty()) return null

        val operatingHour = OperatingHour()

        operatingHourDtos.forEach { dailyDto ->
            val dayOfWeek = ApiConstants.KOR_DAY_MAP.entries.find { it.value == dailyDto.label }?.key
            if (dayOfWeek != null) {
                dailyDto.slots.forEach { slotDto ->
                    if (slotDto.begin.isNotBlank() && slotDto.end.isNotBlank()) {
                        val beginTime = LocalTime.parse(slotDto.begin, timeFormatter)
                        val endTime = if (slotDto.end == "24:00") LocalTime.of(23, 59, 59) else LocalTime.parse(slotDto.end, timeFormatter)

                        operatingHour.addTimeSlot(
                            TimeSlot(dayOfWeek = dayOfWeek, beginTime = beginTime, endTime = endTime)
                        )
                    }
                }
            }
        }
        return if (operatingHour.timeSlots.isEmpty()) null else operatingHour
    }
}