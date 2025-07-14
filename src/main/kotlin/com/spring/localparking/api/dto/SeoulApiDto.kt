package com.spring.localparking.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SeoulApiDto(
    @JsonProperty("privateParkingList")
    val privateParkingList: List<ParkingInfo>
)

data class ParkingInfo(
    @JsonProperty("prk_type") val parkingType: String?,
    @JsonProperty("prk_nm") val parkingName: String,
    @JsonProperty("prk_cd") val parkingCode: String,
    @JsonProperty("cpcty") val capacity: String?,
    @JsonProperty("cur_prk_cnt") val currentParkingCount: String?,
    @JsonProperty("realtime") val isRealtimeEnabled: Boolean,
    @JsonProperty("pay_yn") val isPaid: String,
    @JsonProperty("rates") val baseFee: String?,
    @JsonProperty("time_rates") val baseTime: String?,
    @JsonProperty("add_rates") val additionalFee: String?,
    @JsonProperty("add_time_rates") val additionalTime: String?,
    @JsonProperty("phone") val tel: String?,
    @JsonProperty("address") val address: String?,
    @JsonProperty("lat") val latitude: String?,
    @JsonProperty("lng") val longitude: String?,
    @JsonProperty("weekday_begin_time") val weekdayBeginTime: String?,
    @JsonProperty("weekday_end_time") val weekdayEndTime: String?,
    @JsonProperty("weekend_begin_time") val weekendBeginTime: String?,
    @JsonProperty("weekend_end_time") val weekendEndTime: String?,
    @JsonProperty("holiday_begin_time") val holidayBeginTime: String?,
    @JsonProperty("holiday_end_time") val holidayEndTime: String?
)
