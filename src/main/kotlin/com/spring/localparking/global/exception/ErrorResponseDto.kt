package com.spring.localparking.global.exception


data class ErrorResponseDto (val status: Int, val message : String) {
    companion object {
    fun from(e: CustomException): ErrorResponseDto {
        return ErrorResponseDto(
            status = e.status().value(),
            message = e.message ?: "알 수 없는 오류입니다."
        )
    }
    }
}