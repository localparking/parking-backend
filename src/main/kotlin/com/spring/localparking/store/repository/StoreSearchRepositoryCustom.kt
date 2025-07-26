package com.spring.localparking.store.repository

import com.spring.localparking.store.domain.StoreDocument
import com.spring.localparking.store.dto.StoreSearchRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface StoreSearchRepositoryCustom {
    fun searchByFilters(
        request: StoreSearchRequest,
        categoryFilterIds: List<Long>?,
        pageable: Pageable
    ): Page<StoreDocument>

    fun searchByTextAndLocation(
        query: String,
        lat: Double?,
        lon: Double?,
        distanceKm: Int,
        pageable: Pageable
    ): Page<StoreDocument>
}
