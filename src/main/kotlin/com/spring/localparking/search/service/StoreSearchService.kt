package com.spring.localparking.search.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.category.service.CategoryResolveService
import com.spring.localparking.search.dto.page.PageResponse
import com.spring.localparking.search.dto.page.PageSearchResponse
import com.spring.localparking.search.dto.page.PagingInfo
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.search.dto.StoreListResponse
import com.spring.localparking.search.dto.StoreSearchRequest
import com.spring.localparking.search.domain.StoreDocument
import com.spring.localparking.search.repository.store.StoreSearchRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class StoreSearchService(
    private val storeSearchRepository: StoreSearchRepository,
    private val categoryResolveService: CategoryResolveService
) {
    private val PAGE_SIZE = 30

    fun search(req: StoreSearchRequest): PageResponse<StoreListResponse> {
        val resolvedCategoryIds = categoryResolveService.resolveAll(req.categoryIds)
        val pageable = PageRequest.of(req.page, PAGE_SIZE)
        val searchRequest = if (req.lat == null || req.lon == null) {
            req.copy(lat = 37.498095, lon = 127.027610)
        } else {
            req
        }
        val searchRadiusKm = if (searchRequest.distanceLevel == 2) 4 else 2
        val page = storeSearchRepository.searchByFilters(searchRequest, resolvedCategoryIds, pageable, searchRadiusKm)
        val content = page.content.map { StoreListResponse.of(it) }
        return PageResponse(
            content = content,
            paging = PagingInfo(page = page.number, totalPages = page.totalPages)
        )
    }

    fun searchByText(req: StoreSearchRequest): PageSearchResponse<StoreListResponse> {
        if (req.query.isNullOrBlank()) {
            throw CustomException(ErrorCode.SEARCH_NOT_BLANK)
        }
        val expandedQuery = req.query?.takeIf { it.isNotBlank() }?.let {
            categoryResolveService.resolveCategoryNameToQuery(it)
        }
        val searchRequest = req.copy(query = expandedQuery)
        val resolvedCategoryIds = categoryResolveService.resolveAll(req.categoryIds)
        val pageable = PageRequest.of(req.page, PAGE_SIZE)

        val finalLat = req.lat ?: 37.498095
        val finalLon = req.lon ?: 127.027610

        var page: Page<StoreDocument>
        var searchRadiusKm = 2
        while (true) {
            page = storeSearchRepository.searchByText(searchRequest.copy(lat = finalLat, lon = finalLon), resolvedCategoryIds, pageable, searchRadiusKm)
            if (page.hasContent() || searchRadiusKm >= 10) {
                break
            }
            searchRadiusKm += 2
        }

        val content = page.content.map { StoreListResponse.of(it) }

        return PageSearchResponse(
            content = content,
            paging = PagingInfo(page = page.number, totalPages = page.totalPages),
            searchRadiusKm = searchRadiusKm
        )
    }
}