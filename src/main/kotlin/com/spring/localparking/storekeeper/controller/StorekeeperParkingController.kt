package com.spring.localparking.storekeeper.controller

import com.spring.localparking.auth.dto.storekeeper.ParkingLotManualRequestDto
import com.spring.localparking.auth.exception.UnauthorizedException
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.storekeeper.dto.AssociatedParkingLotResponseDto
import com.spring.localparking.storekeeper.dto.LinkParkingRequestDto
import com.spring.localparking.storekeeper.service.StorekeeperService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "점주 주차장 관리 컨트롤러", description = "점주가 자신의 가게에 연계된 주차장을 관리하는 API입니다.")
@RestController
@RequestMapping("/storekeeper")
class StorekeeperParkingController(
    private val storekeeperService: StorekeeperService
) {

    @Operation(summary = "연계된 주차장 목록 조회", description = "내 가게에 연계된 모든 주차장 목록을 조회합니다.")
    @GetMapping("/parking")
    fun getAssociatedParkingLots(
        @AuthenticationPrincipal principal: CustomPrincipal
    ): ResponseEntity<ResponseDto<List<AssociatedParkingLotResponseDto>>> {
        val userId = principal.id ?: throw UnauthorizedException()
        val parkingLots = storekeeperService.getAssociatedParkingLots(userId)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, parkingLots))
    }

    @Operation(summary = "주차장과 내 가게 연결", description = "주차장 코드를 사용하여 기존 주차장을 내 가게에 연결합니다.")
    @PostMapping("/parking/link")
    fun linkParkingLot(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @Valid @RequestBody request: LinkParkingRequestDto,
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.linkParkingLot(userId, request)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }

    @Operation(summary = "연계된 주차장 정보 수정", description = "내 가게에 연계된 주차장의 정보를 수정합니다.")
    @PutMapping("/parking/{parkingCode}")
    fun updateAssociatedParkingLot(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable parkingCode: String,
        @Valid @RequestBody request: ParkingLotManualRequestDto
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.updateAssociatedParkingLot(userId, parkingCode, request)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }

    @Operation(summary = "가게와 주차장 연결 해제", description = "내 가게에 연계된 주차장과의 연결을 끊습니다. (가게에 최소 1개의 주차장은 연결되어 있어야 합니다.)")
    @DeleteMapping("/parking/{parkingCode}/unlink")
    fun unlinkParkingLot(
        @AuthenticationPrincipal principal: CustomPrincipal,
        @PathVariable parkingCode: String
    ): ResponseEntity<ResponseDto<Unit>> {
        val userId = principal.id ?: throw UnauthorizedException()
        storekeeperService.unlinkParkingLot(userId, parkingCode)
        return ResponseEntity.ok(ResponseDto.empty(SuccessCode.OK))
    }

}