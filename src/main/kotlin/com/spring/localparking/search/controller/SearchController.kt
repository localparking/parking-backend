package com.spring.localparking.search.controller

import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.search.dto.SearchSuggestionResponse
import com.spring.localparking.search.service.SearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "검색 컨트롤러", description = "추천/최근 검색 관련 API입니다.")
@RestController
@RequestMapping("/search")
class SearchController(
    private val searchService: SearchService
) {
    @Operation(summary = "검색 제안 조회 (추천 검색어 + 최근 검색어)", description = "로그인한 사용자에 대한 추천 검색어와 최근 검색어를 조회합니다. ")
    @GetMapping("/suggestions")
    fun getSearchSuggestions(
        @AuthenticationPrincipal principal: CustomPrincipal?
    ): ResponseEntity<ResponseDto<SearchSuggestionResponse>> {
        val userId = principal?.id
        val suggestions = searchService.getSearchSuggestions(userId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, suggestions))
    }

    @Operation(summary = "최근 검색어 삭제", description = "로그인한 사용자의 최근 검색어를 삭제합니다.")
    @DeleteMapping("/recent/{searchId}")
    fun deleteRecentSearch(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable searchId: Long
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = requireNotNull(principal.id) { throw UnauthorizedException() }
        searchService.deleteRecentSearch(userId, searchId)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }

    @Operation(summary = "최근 검색어 전체 삭제", description = "로그인한 사용자의 모든 최근 검색어를 삭제합니다.")
    @DeleteMapping("/recent")
    fun deleteAllRecentSearches(
        @AuthenticationPrincipal principal: CustomPrincipal
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = requireNotNull(principal.id) { throw UnauthorizedException() }
        searchService.deleteAllRecentSearches(userId)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
}