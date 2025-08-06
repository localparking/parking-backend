package com.spring.localparking.search.dto.naver

data class NaverApiResponse(
    val lastBuildDate: String,
    val total: Int,
    val start: Int,
    val display: Int,
    val items: List<NaverSearchItemDto>
)