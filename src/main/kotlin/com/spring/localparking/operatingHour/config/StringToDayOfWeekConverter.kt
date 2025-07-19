package com.spring.localparking.operatingHour.config

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import java.time.DayOfWeek

@ReadingConverter
class StringToDayOfWeekConverter : Converter<String, DayOfWeek> {
    override fun convert(source: String): DayOfWeek {
        return DayOfWeek.valueOf(source)
    }
}