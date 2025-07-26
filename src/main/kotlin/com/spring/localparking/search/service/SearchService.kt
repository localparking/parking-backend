package com.spring.localparking.search.service
import com.spring.global.exception.ErrorCode
import com.spring.localparking.auth.exception.AccessDeniedException
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.search.domain.RecentSearch
import com.spring.localparking.search.dto.RecentSearchResponse
import com.spring.localparking.search.dto.SearchSuggestionResponse
import com.spring.localparking.search.repository.RecentSearchRepository
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class SearchService(
    private val recentSearchRepository: RecentSearchRepository,
    private val userRepository: UserRepository
) {
    fun getSearchSuggestions(userId: Long?): SearchSuggestionResponse {
        val recommendations = getRecommendations()
        val recentSearches = if (userId != null) {
            getRecentSearches(userId)
        } else {
            emptyList()
        }
        return SearchSuggestionResponse(
            recentSearches = recentSearches,
            recommendations = recommendations
        )
    }

    private fun getRecommendations(): List<String> {
        // TODO: 향후 DB나 설정 파일에서 관리
        return listOf("강남역 주차장", "음식점", "강남역 양식", "교보")
    }

    private fun getRecentSearches(userId: Long): List<RecentSearchResponse> {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        return recentSearchRepository.findByUserOrderByCreatedAtDesc(user)
            .map { RecentSearchResponse.from(it) }
    }

    @Transactional
    fun addRecentSearch(userId: Long, query: String) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val existingSearch = recentSearchRepository.findByUserAndQuery(user, query)
        if (existingSearch != null) {
            recentSearchRepository.delete(existingSearch)
        }
        val newSearch = RecentSearch(user = user, query = query)
        recentSearchRepository.save(newSearch)
        val searches = recentSearchRepository.findByUserOrderByCreatedAtDesc(user)
        if (searches.size > 10) {
            recentSearchRepository.delete(searches.last())
        }
    }

    @Transactional
    fun deleteRecentSearch(userId: Long, searchId: Long) {
        val search = recentSearchRepository.findById(searchId)
            .orElseThrow { CustomException(ErrorCode.SEARCH_NOT_FOUND) }
        if (search.user.id != userId) {
            throw AccessDeniedException()
        }
        recentSearchRepository.delete(search)
    }

    @Transactional
    fun deleteAllRecentSearches(userId: Long) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        recentSearchRepository.deleteByUser(user)
    }
}