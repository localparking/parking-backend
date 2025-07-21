package com.spring.localparking.store.domain

import com.spring.localparking.operatingHour.domain.DocumentOperatingHour
import jakarta.persistence.Id
import org.springframework.data.elasticsearch.annotations.*
import org.springframework.data.elasticsearch.core.geo.GeoPoint

@Document(indexName = "store")
//@Setting(settingPath = "elasticsearch/elasticsearch-settings.json")
data class StoreDocument (
    @Id
    val id : Long,
    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "nori"),
        otherFields = [
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val name: String,
    @Field(type = FieldType.Keyword)
    val categoryIds: List<Long> = emptyList(),
    @Field(type = FieldType.Keyword)
    val categoryNames: List<String> = emptyList(),

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "nori"),
        otherFields = [
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val fullDoroAddress: String?= null,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "nori"),
        otherFields = [
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val fullJibeonAddress: String?= null,

    @Field(type = FieldType.Keyword)
    val sido: String?= null,

    @Field(type = FieldType.Keyword)
    val sigungu: String?= null,

    @Field(type = FieldType.Boolean)
    val isCoalition: Boolean = false,

    @Field(type = FieldType.Integer)
    val maxFreeMin: Int? = null,

    @GeoPointField
    val location: GeoPoint,

    @Field(type = FieldType.Boolean)
    val isOpen: Boolean? = null,
    @Field(type = FieldType.Boolean)
    val is24Hours: Boolean = false,

    @Field(type = FieldType.Nested)
    val operatingHours: List<DocumentOperatingHour> = listOf()
)