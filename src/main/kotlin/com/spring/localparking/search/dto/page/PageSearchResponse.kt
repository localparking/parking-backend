package com.spring.localparking.search.dto.page


data class PageSearchResponse<T>(
    val content: List<T>,
    val paging: PagingInfo,
    val searchRadiusKm: Int? = null
)