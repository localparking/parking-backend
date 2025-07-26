package com.spring.localparking.search.dto

import com.spring.localparking.search.domain.RecentSearch
import java.time.format.DateTimeFormatter

data class RecentSearchResponse(
    val id: Long,
    val query: String,
    val date: String
) {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MM.dd")
        fun from(entity: RecentSearch): RecentSearchResponse {
            return RecentSearchResponse(
                id = entity.id!!,
                query = entity.query,
                date = entity.createdAt.format(DATE_FORMATTER)
            )
        }
    }
}