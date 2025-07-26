package com.spring.localparking.parking.domain

import com.spring.localparking.operatingHour.domain.DocumentOperatingHour
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*
import org.springframework.data.elasticsearch.core.geo.GeoPoint

@Document(indexName = "parking_lot")
data class ParkingLotDocument(
    @Id
    val parkingCode: String,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "nori"),
        otherFields = [InnerField(suffix = "keyword", type = FieldType.Keyword)]
    )
    val name: String,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "nori"),
        otherFields = [InnerField(suffix = "keyword", type = FieldType.Keyword)]
    )
    val address: String?,

    val location: GeoPoint,

    @Field(type = FieldType.Boolean)
    val isFree: Boolean,

    @Field(type = FieldType.Boolean)
    val isRealtime: Boolean,

    @Field(type = FieldType.Boolean)
    val isOpen: Boolean? = null,

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