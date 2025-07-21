package com.spring.localparking.operatingHour

import com.spring.localparking.operatingHour.domain.OperatingHour
import com.spring.localparking.operatingHour.domain.normalizedSlots
import com.spring.localparking.operatingHour.domain.NormalizedSlot
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm")
private val KOR_DAY = mapOf(
    DayOfWeek.MONDAY to "월",
    DayOfWeek.TUESDAY to "화",
    DayOfWeek.WEDNESDAY to "수",
    DayOfWeek.THURSDAY to "목",
    DayOfWeek.FRIDAY to "금",
    DayOfWeek.SATURDAY to "토",
    DayOfWeek.SUNDAY to "일"
)

data class OperatingSlotDto(
    val begin: String,
    val end: String,
)

data class DailyOperatingDto(
    val dayLabel: String,
    val slots: List<OperatingSlotDto>
)

object OperatingHourPresenter {

    fun build(op: OperatingHour?): List<DailyOperatingDto> {
        if (op == null || op.timeSlots.isEmpty()) {
            return DayOfWeek.values().map { emptyDay(it) }
        }
        val grouped = op.normalizedSlots().groupBy { it.dayOfWeek }

        return DayOfWeek.values().map { dow ->
            val raw = grouped[dow].orEmpty()
            if (raw.isEmpty()) return@map emptyDay(dow)

            val (normal, overnight) = raw.partition { !it.overnight }
            val merged = mergeNormalSameDay(normal)
            val ordered = (merged + overnight)
                .sortedWith(compareBy<NormalizedSlot> { it.begin }.thenBy { it.end })

            DailyOperatingDto(
                dayLabel = KOR_DAY[dow] ?: dow.name,
                slots = ordered.map {
                    OperatingSlotDto(
                        begin = it.begin.format(TIME_FMT),
                        end = it.end.format(TIME_FMT)
                    )
                },
            )
        }
    }

    private fun emptyDay(dow: DayOfWeek) = DailyOperatingDto(
        dayLabel = KOR_DAY[dow] ?: dow.name,
        slots = emptyList(),
    )

    private fun mergeNormalSameDay(list: List<NormalizedSlot>): List<NormalizedSlot> {
        if (list.isEmpty()) return list
        val sorted = list.sortedBy { it.begin }
        val merged = mutableListOf<NormalizedSlot>()
        var cur = sorted.first()
        for (i in 1 until sorted.size) {
            val nxt = sorted[i]
            if (!cur.overnight && !nxt.overnight &&
                cur.dayOfWeek == nxt.dayOfWeek &&
                cur.end >= nxt.begin
            ) {
                cur = cur.copy(end = maxOf(cur.end, nxt.end))
            } else {
                merged += cur
                cur = nxt
            }
        }
        merged += cur
        return merged
    }
}
