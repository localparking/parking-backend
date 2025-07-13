package com.spring.localparking.parking.domain

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

    val weekdayBeginTime: String?=null,
    val weekdayEndTime: String?=null,
    val weekendBeginTime: String?=null,
    val weekendEndTime: String?=null,
    val holidayBeginTime: String?=null,
    val holidayEndTime: String?=null
) {
    fun isOpenNow(): Boolean {
        val now = LocalTime.now()
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek
        val isHoliday = isHoliday(today)

        val (beginTimeStr, endTimeStr) = when {
            isHoliday -> holidayBeginTime to holidayEndTime
            dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY -> weekendBeginTime to weekendEndTime
            else -> weekdayBeginTime to weekdayEndTime
        }

        if (beginTimeStr == null || endTimeStr == null) return true

        val openTime = parseTime(beginTimeStr) ?: return true
        val closeTime = parseTime(endTimeStr) ?: return true

        if (openTime == LocalTime.MIN && closeTime == LocalTime.MIN) return true

        return !now.isBefore(openTime) && now.isBefore(closeTime)
    }

    private fun parseTime(timeStr: String): LocalTime? {
        if (timeStr.length != 4) return null
        // "2400"은 다음날 0시를 의미하므로, 하루의 끝이 아닌 시작으로 처리
        if (timeStr == "2400") return LocalTime.MIN
        return try {
            val hour = timeStr.substring(0, 2).toInt()
            val minute = timeStr.substring(2, 4).toInt()
            LocalTime.of(hour, minute)
        } catch (e: Exception) {
            null
        }
    }
    //임시
    private fun isHoliday(date: LocalDate): Boolean {
        return false
    }
}
