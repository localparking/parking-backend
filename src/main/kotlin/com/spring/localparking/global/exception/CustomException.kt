package com.spring.localparking.global.exception

import com.spring.global.exception.ErrorCode
import org.springframework.http.HttpStatus

open class CustomException (
    val errorCode : ErrorCode
) : RuntimeException(errorCode.message) {
    fun status() : HttpStatus = errorCode.status
}