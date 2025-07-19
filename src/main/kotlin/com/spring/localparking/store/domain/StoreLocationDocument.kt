package com.spring.localparking.store.domain

import jakarta.persistence.Id
import org.springframework.data.elasticsearch.annotations.*
import org.springframework.data.elasticsearch.core.geo.GeoPoint

@Document(indexName = "store_location")
@Setting(settingPath = "elasticsearch/nori-settings.json")
class StoreLocationDocument (
    @Id
    var storeId: Long,
    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "nori"),
        otherFields = [InnerField(suffix = "keyword", type = FieldType.Keyword)]
    )
    val storeName: String,

    @Field(type = FieldType.Text, analyzer = "nori")
    val fullDoroAddress: String?= null,

    @Field(type = FieldType.Text, analyzer = "nori")
    val fullJibeonAddress: String?= null,

    @Field(type = FieldType.Keyword)
    val sido: String?= null,

    @Field(type = FieldType.Keyword)
    val sigungu: String?= null,

    val location: GeoPoint
){
    companion object {
        fun from(store: Store): StoreLocationDocument =
            StoreLocationDocument(
                storeId = store.id,
                storeName = store.name,
                fullDoroAddress = store.location?.doroAddress?.fullAddress,
                fullJibeonAddress = store.location?.jibeonAddress?.fullAddress,
                sido = store.location?.doroAddress?.sido ,
                sigungu = store.location?.doroAddress?.sigungu,
                location = GeoPoint(store.location!!.lat!!, store.location!!.lon!!)
            )
    }
}