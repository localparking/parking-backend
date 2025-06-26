package com.spring.localparking.global.response

data class ResponseDto<T> (val message: String, val data: T?){
    companion object {
        fun <T> from(successCode: SuccessCode, data: T): ResponseDto<T> =
            ResponseDto(successCode.message, data)

        fun <T> empty(successCode: SuccessCode): ResponseDto<T> =
            ResponseDto(successCode.message, null)
    }
}