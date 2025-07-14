package com.spring.localparking.parking.domain

import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.DayOfWeek

data class DocumentOperatingHour(

    @Field(type = FieldType.Keyword)
    val dayOfWeek: DayOfWeek?,

    @Field(type = FieldType.Integer)
    val beginTime: Int?,

    @Field(type = FieldType.Integer)
    val endTime: Int?,

    @Field(type = FieldType.Boolean)
    val isOvernight: Boolean = false

)