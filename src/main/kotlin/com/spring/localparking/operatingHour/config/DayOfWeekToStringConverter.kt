package com.spring.localparking.operatingHour.config

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import java.time.DayOfWeek

@WritingConverter
class DayOfWeekToStringConverter : Converter<DayOfWeek, String> {
    override fun convert(source: DayOfWeek): String {
        return source.name
    }
}