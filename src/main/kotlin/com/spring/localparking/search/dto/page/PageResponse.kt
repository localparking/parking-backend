package com.spring.localparking.search.dto.page

data class PageResponse<T>(
    val content: List<T>,
    val paging: PagingInfo
)