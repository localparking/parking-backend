package com.spring.localparking.store.controller

import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.store.dto.*
import com.spring.localparking.store.service.StoreService
import com.spring.localparking.storekeeper.dto.ProductResponseDto
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
    @Operation(summary = "가게 상세 정보 조회", description = "가게 상세 정보를 조회하는 API입니다.")
    @GetMapping("/detail/{storeId}")
    fun getStoreDetail(@PathVariable storeId: Long):
        ResponseEntity<ResponseDto<StoreDetailResponse>> {
        val detail = storeService.getDetail(storeId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, detail))
    }
    @Operation(summary = "가게 상품 주문 폼 조회", description = "특정 가게에 대한 상품 주문 폼을 조회하는 API입니다.")
    @GetMapping("/{storeId}/products")
    fun getStoreProducts(@PathVariable storeId: Long): ResponseEntity<ResponseDto<OrderFormResponseDto>> {
        val results = storeService.getOrderForm(storeId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, results))
    }
}
