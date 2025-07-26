package com.spring.localparking.search.dto

data class PageResponse<T>(
    val content: List<T>,
    val paging: PagingInfo
)