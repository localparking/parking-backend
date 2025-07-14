package com.spring.localparking.parking.dto

import com.spring.localparking.parking.domain.TimeSlot
import java.time.format.DateTimeFormatter

data class OperatingHoursDto(
    val type: String,
    val beginTime: String?,
    val endTime: String?
) {
    companion object {
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

        fun from(type: String, timeSlot: TimeSlot?): OperatingHoursDto {
            if (timeSlot == null) {
                return OperatingHoursDto(type = type, beginTime = null, endTime = null)
            }
            return OperatingHoursDto(
                type = type,
                beginTime = timeSlot?.beginTime?.format(TIME_FORMATTER),
                endTime = timeSlot?.endTime?.format(TIME_FORMATTER)
            )
        }
    }
}