package com.spring.localparking.global.config

import com.spring.global.exception.ErrorCode
import com.spring.localparking.global.dto.SortType
import com.spring.localparking.global.exception.CustomException
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToSortTypeConverter : Converter<String, SortType> {
    override fun convert(source: String): SortType {
        return try {
            SortType.valueOf(source.uppercase())
        } catch (e: CustomException) {
            throw CustomException(ErrorCode.SORT_TYPE_NOT_FOUND)
        }
    }
}