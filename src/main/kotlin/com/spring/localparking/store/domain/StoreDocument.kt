package com.spring.localparking.store.domain

import com.spring.localparking.operatingHour.domain.DocumentOperatingHour
import jakarta.persistence.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.InnerField
import org.springframework.data.elasticsearch.annotations.Setting
import org.springframework.data.elasticsearch.core.geo.GeoPoint

@Document(indexName = "store")
@Setting(settingPath = "elasticsearch/store_index_settings.json")
data class StoreDocument (
    @Id
    val id : Long,
    @org.springframework.data.elasticsearch.annotations.MultiField(
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

    //대표 카테고리
    @Field(type = FieldType.Keyword)
    val primaryCategoryId: Long? = null,
    @Field(type = FieldType.Keyword)
    val primaryCategoryName: String? = null,

    @org.springframework.data.elasticsearch.annotations.MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "nori"),
        otherFields = [
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val fullDoroAddress: String?= null,

    @org.springframework.data.elasticsearch.annotations.MultiField(
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

    val maxFreeMin: Int? = null,

    val location: GeoPoint,

    @Field(type = FieldType.Nested)
    val operatingHours: List<DocumentOperatingHour> = listOf()
)