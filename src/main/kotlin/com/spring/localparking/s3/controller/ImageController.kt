package com.spring.localparking.s3.controller

import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.s3.dto.PresignedUrlResponse
import com.spring.localparking.s3.service.S3PresignedUrlService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "이미지 처리 컨트롤러", description = "이미지 Presigned URL 발급 API입니다.")
@RestController
@RequestMapping("/storekeeper/image")
class ImageController(
    private val s3PresignedUrlService: S3PresignedUrlService
) {

    @Operation(
        summary = "상품 이미지 업로드를 위한 Presigned URL 발급",
        description = "S3에 직접 파일을 올릴 수 있는 10분짜리 임시 URL과 파일 키를 발급하는 API"
    )
    @GetMapping("/presigned-url")
    fun getProductImagePresignedUrl(
        @RequestParam("filename") originalFilename: String
    ): ResponseEntity<ResponseDto<PresignedUrlResponse>> {
        val response = s3PresignedUrlService.getPresignedUrl(originalFilename)
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, response))
    }
}