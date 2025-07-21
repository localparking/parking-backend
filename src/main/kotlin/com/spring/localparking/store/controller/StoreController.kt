package com.spring.localparking.store.controller

import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.dto.PageResponse
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.store.dto.StoreDetailResponse
import com.spring.localparking.store.dto.StoreListResponse
import com.spring.localparking.store.dto.StoreSearchRequest
import com.spring.localparking.store.service.StoreService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "가게 컨트롤러", description = "가게 관련 API입니다.")
@RestController
@RequestMapping("/store")
class StoreController(
    private val storeService: StoreService
) {
    @Operation(summary = "지도 기반 주차장 검색", description = "지도에서 주차장을 검색하는 API입니다.")
    @PostMapping("/map/search")
    fun search(@AuthenticationPrincipal principal: CustomPrincipal, @RequestBody request: StoreSearchRequest):
        ResponseEntity<ResponseDto<PageResponse<StoreListResponse>>> {
        val results = storeService.search(request)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, results))
    }
    @Operation(summary = "가게 상세 정보 조회", description = "가게 상세 정보를 조회하는 API입니다.")
    @GetMapping("/detail/{storeId}")
    fun getStoreDetail(@AuthenticationPrincipal principal: CustomPrincipal, @PathVariable storeId: Long):
        ResponseEntity<ResponseDto<StoreDetailResponse>> {
        val detail = storeService.getDetail(storeId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, detail))
    }
}
