package com.spring.localparking.global.dto


data class PageSearchResponse<T>(
    val content: List<T>,
    val paging: PagingInfo,
    val searchRadiusKm: Int? = null
)