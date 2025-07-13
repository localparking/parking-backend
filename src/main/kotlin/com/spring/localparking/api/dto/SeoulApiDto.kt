package com.spring.localparking.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SeoulApiDto(
    @JsonProperty("CITYDATA")
    val cityData: CityData
) {
    data class CityData(
        @JsonProperty("PRK_STTS")
        val parkingStatus: List<ParkingInfo>
    )
    data class ParkingInfo(
        @JsonProperty("PRK_NM") val parkingName: String,
        @JsonProperty("PRK_CD") val parkingCode: String,
        @JsonProperty("PRK_TYPE") val parkingType: String?,
        @JsonProperty("CPCTY") val capacity: String?,
        @JsonProperty("CUR_PRK_CNT") val currentParkingCount: String?,
        @JsonProperty("CUR_PRK_YN") val isRealtimeEnabled: String,
        @JsonProperty("PAY_YN") val isPaid: String,
        @JsonProperty("RATES") val baseFee: String?,
        @JsonProperty("TIME_RATES") val baseTime: String?,
        @JsonProperty("ADD_RATES") val additionalFee: String?,
        @JsonProperty("ADD_TIME_RATES") val additionalTime: String?,
        @JsonProperty("ADDRESS") val address: String?,
        @JsonProperty("LAT") val latitude: String?,
        @JsonProperty("LNG") val longitude: String?
    )
}