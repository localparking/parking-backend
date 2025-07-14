package com.spring.localparking.parking.domain

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.core.geo.GeoPoint

@Document(indexName = "parking_lot")
data class ParkingLotDocument(
    @Id
    val parkingCode: String,

    @Field(type = FieldType.Text)
    val name: String,

    @Field(type = FieldType.Keyword)
    val address: String?,

    val location: GeoPoint,

    @Field(type = FieldType.Boolean)
    val isFree: Boolean,

    @Field(type = FieldType.Boolean)
    val isRealtime: Boolean,

    @Field(type = FieldType.Boolean)
    val is24Hours: Boolean,

    @Field(type = FieldType.Keyword)
    val congestion: String? = null,

    @Field(type = FieldType.Integer)
    val capacity: Int?,

    @Field(type = FieldType.Integer)
    val baseFee: Int?,

    @Field(type = FieldType.Integer)
    val baseTimeMin: Int?,

    @Field(type = FieldType.Integer)
    val hourlyFee: Int?,

    @Field(type = FieldType.Nested)
    val operatingHours: List<DocumentOperatingHour> = listOf()
)