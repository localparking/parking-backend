package com.spring.localparking.search.controller

import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.search.dto.naver.SearchItemResponse
import com.spring.localparking.search.service.NaverSearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "검색 컨트롤러", description = "검색 관련 API입니다.")
@RestController
@RequestMapping("/text-search")
class SearchController(private val naverSearchService: NaverSearchService) {

    @Operation(summary = "네이버 검색 API를 이용한 텍스트 검색", description = "검색어로 네이버 API를 통해 장소를 검색합니다.")
    @GetMapping
    fun search(@RequestParam query: String):
            ResponseEntity<ResponseDto<List<SearchItemResponse>>> {
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, naverSearchService.search(query)))
    }
}