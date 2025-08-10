package com.spring.localparking.admin.controller

import com.spring.localparking.admin.dto.StorekeeperRequestResponse
import com.spring.localparking.admin.service.AdminStoreService
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.dto.RequestStatus
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "관리자 컨트롤러", description = "관리자의 점주 신청 관리 관련 API입니다.")
@RestController
@RequestMapping("/admin")
class AdminStoreController (
    private val adminService: AdminStoreService
){
    @Operation(summary = "점주 가입 신청 목록 조회", description = "관리자가 상태(PENDING, APPROVED, REJECTED)에 따라 가입 신청 목록을 조회합니다.")
    @GetMapping("/storekeeper-requests")
    fun getStorekeeperRequests(@AuthenticationPrincipal principal: CustomPrincipal,
                               @RequestParam status: RequestStatus):
            ResponseEntity<ResponseDto<List<StorekeeperRequestResponse>>> {
        val requests = adminService.getStorekeeperRequests(status)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, requests))
    }

    @Operation(summary = "점주 가입 신청 승인", description = "관리자가 특정 가입 신청을 승인 처리합니다.")
    @PostMapping("/storekeeper-requests/{adminId}/approve")
    fun approveStorekeeperRequest(@AuthenticationPrincipal principal: CustomPrincipal,
                                  @PathVariable adminId: String): ResponseEntity<ResponseDto<Unit>> {
        adminService.processStorekeeperRequest(adminId, true)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }

    @Operation(summary = "점주 가입 신청 거절", description = "관리자가 특정 가입 신청을 거절 처리합니다.")
    @PostMapping("/storekeeper-requests/{adminId}/reject")
    fun rejectStorekeeperRequest(@AuthenticationPrincipal principal: CustomPrincipal,
                                 @PathVariable adminId: String): ResponseEntity<ResponseDto<Unit>> {
        adminService.processStorekeeperRequest(adminId, false)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }
}