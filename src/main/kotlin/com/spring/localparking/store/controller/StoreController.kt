package com.spring.localparking.store.controller

import com.spring.localparking.search.dto.PageResponse
import com.spring.localparking.search.dto.PageSearchResponse
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.store.dto.*
import com.spring.localparking.store.service.StoreService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "가게 컨트롤러", description = "가게 관련 API입니다.")
@RestController
@RequestMapping("/store")
class StoreController(
    private val storeService: StoreService
) {
    @Operation(summary = "지도 기반 가게 검색", description = "지도에서 가게를 검색하는 API입니다.")
    @PostMapping("/map-search")
    fun search(@RequestBody request: StoreSearchRequest):
        ResponseEntity<ResponseDto<PageResponse<StoreListResponse>>> {
        val results = storeService.search(request)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, results))
    }
    @Operation(summary = "가게 상세 정보 조회", description = "가게 상세 정보를 조회하는 API입니다.")
    @GetMapping("/detail/{storeId}")
    fun getStoreDetail(@PathVariable storeId: Long):
        ResponseEntity<ResponseDto<StoreDetailResponse>> {
        val detail = storeService.getDetail(storeId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, detail))
    }
    @Operation(summary = "텍스트 기반 가게 검색", description = "검색어로 가게를 검색하는 API입니다.")
    @PostMapping("/text-search")
    fun searchByText(@RequestBody request: StoreSearchRequest):
            ResponseEntity<ResponseDto<PageSearchResponse<StoreListResponse>>> {
        val results = storeService.searchByText(request)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, results))
    }
    @Operation(summary = "가게 상품 목록 조회", description = "특정 가게에 등록된 상품 목록을 조회합니다.")
    @GetMapping("/{storeId}/products")
    fun getStoreProducts(@PathVariable storeId: Long): ResponseEntity<ResponseDto<List<ProductResponseDto>>> {
        val products = storeService.getProductsByStore(storeId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, products))
    }
}
