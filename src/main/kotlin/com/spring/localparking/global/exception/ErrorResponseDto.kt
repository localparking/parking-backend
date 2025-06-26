package com.spring.localparking.global.exception

data class ErrorResponseDto (val message : String) {
    companion object {
        fun from(e: CustomException) = ErrorResponseDto(e.message?:"알 수 없는 오류입니다.")
    }
}