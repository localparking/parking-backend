package com.spring.localparking.search.dto

data class SearchItemResponse(
    val title: String,
    val roadAddress: String,
    val lat: Double,
    val lon: Double
)