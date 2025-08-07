package com.spring.localparking.storekeeper.controller

import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.storekeeper.dto.MyStoreResponse
import com.spring.localparking.storekeeper.dto.MyStoreUpdateRequest
import com.spring.localparking.storekeeper.service.StorekeeperService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "점주 가게 관리 컨트롤러", description = "점주가 자신의 가게 정보를 관리하는 API입니다.")
@RestController
@RequestMapping("/storekeeper")
class StorekeeperController(
    private val storekeeperService: StorekeeperService
) {
    @Operation(summary = "내 가게 정보 조회", description = "점주의 가게 정보를 조회합니다.")
    @GetMapping("/my-store")
    fun getMyStoreInfo(
        @AuthenticationPrincipal principal: CustomPrincipal
    ): ResponseEntity<ResponseDto<MyStoreResponse>> {
        val userId = principal.id ?: throw UnauthorizedException()
        val response = storekeeperService.getMyStoreInfo(userId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, response))
    }

    @Operation(summary = "내 가게 정보 수정", description = "점주의 가게 정보를 수정합니다.")
    @PutMapping("/my-store")
    fun updateMyStoreInfo(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @RequestBody request: MyStoreUpdateRequest
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.updateMyStoreInfo(userId, request)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
}