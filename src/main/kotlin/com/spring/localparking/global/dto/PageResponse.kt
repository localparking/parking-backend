package com.spring.localparking.global.dto

data class PageResponse<T>(
    val content: List<T>,
    val paging: PagingInfo
)