package com.spring.localparking.search.controller

import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.search.dto.page.PageResponse
import com.spring.localparking.search.dto.page.PageSearchResponse
import com.spring.localparking.search.dto.StoreListResponse
import com.spring.localparking.search.dto.StoreSearchRequest
import com.spring.localparking.search.dto.StoreSimpleResponse
import com.spring.localparking.search.service.StoreSearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "가게 컨트롤러", description = "가게 관련 API입니다.")
@RestController
@RequestMapping("/store")
class StoreSearchController(
    private val storeSearchService: StoreSearchService
) {
    @Operation(summary = "지도 기반 가게 검색", description = "지도에서 가게를 검색하는 API입니다.")
    @PostMapping("/map-search")
    fun search(@RequestBody request: StoreSearchRequest):
            ResponseEntity<ResponseDto<PageResponse<StoreListResponse>>> {
        val results = storeSearchService.search(request)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, results))
    }
    @Operation(summary = "텍스트 기반 가게 검색", description = "검색어로 가게를 검색하는 API입니다.")
    @PostMapping("/text-search")
    fun searchByText(@RequestBody request: StoreSearchRequest):
            ResponseEntity<ResponseDto<PageSearchResponse<StoreListResponse>>> {
        val results = storeSearchService.searchByText(request)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, results))
    }

    @Operation(summary = "점주 가입을 위한 가게 이름 검색", description = "필터 없이 가게 이름으로만 전체 가게를 검색하는 API입니다.")
    @GetMapping("/text-search/name")
    fun searchForRegistration(@RequestParam query: String):
            ResponseEntity<ResponseDto<List<StoreSimpleResponse>>> {
        val results = storeSearchService.searchByNameForRegistration(query)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, results))
    }
}