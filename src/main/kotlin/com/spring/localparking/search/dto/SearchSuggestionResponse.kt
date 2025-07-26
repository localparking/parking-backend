package com.spring.localparking.search.dto

data class SearchSuggestionResponse(
    val recommendations: List<String>,
    val recentSearches: List<RecentSearchResponse>
)