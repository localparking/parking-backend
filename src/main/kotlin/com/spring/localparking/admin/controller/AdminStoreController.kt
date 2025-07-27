package com.spring.localparking.admin.controller

import com.spring.localparking.admin.dto.StoreOwnershipReqResponse
import com.spring.localparking.admin.service.AdminService
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.dto.RequestStatus
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "관리자 가게 컨트롤러", description = "관리자 가게 관련 API입니다.")
@RestController
@RequestMapping("/admin/store")
class AdminStoreController (
    private val adminStoreService: AdminService
){
    @Operation(summary = "소유권 신청 목록 조회", description = "관리자가 소유권 신청 목록을 조회합니다.")
    @GetMapping("/ownership-requests")
    fun getOwnershipRequests(@AuthenticationPrincipal principal: CustomPrincipal,
                             @RequestParam status: RequestStatus):
            ResponseEntity<ResponseDto<List<StoreOwnershipReqResponse>>> {
        val requests = adminStoreService.getOwnershipRequests(status)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK,requests))
    }

    @Operation(summary = "소유권 신청 승인", description = "관리자가 특정 소유권 신청을 승인 처리합니다.")
    @PostMapping("/ownership-requests/{requestId}/approve")
    fun approveOwnershipRequest(@AuthenticationPrincipal principal: CustomPrincipal,
                                @PathVariable requestId: Long): ResponseEntity<ResponseDto<Unit>> {
        adminStoreService.processOwnershipRequest(requestId, true)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }

    @Operation(summary = "소유권 신청 거절", description = "관리자가 특정 소유권 신청을 거절 처리합니다.")
    @PostMapping("/ownership-requests/{requestId}/reject")
    fun rejectOwnershipRequest(@AuthenticationPrincipal principal: CustomPrincipal,
                               @PathVariable requestId: Long): ResponseEntity<ResponseDto<Unit>> {
        adminStoreService.processOwnershipRequest(requestId, false)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
}