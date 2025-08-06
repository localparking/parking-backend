package com.spring.localparking.search.repository.store

import com.spring.localparking.search.domain.StoreDocument
import com.spring.localparking.search.dto.StoreSearchRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface StoreSearchRepositoryCustom {
    fun searchByFilters(
        request: StoreSearchRequest,
        categoryFilterIds: List<Long>?,
        pageable: Pageable,
        searchRadiusKm: Int
    ): Page<StoreDocument>

    fun searchByText(
        request: StoreSearchRequest,
        categoryFilterIds: List<Long>?,
        pageable: Pageable,
        searchRadiusKm: Int
    ): Page<StoreDocument>
}
