package com.spring.localparking.global.exception

import com.spring.global.exception.ErrorCode
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.PrintWriter
import java.io.StringWriter
import java.rmi.server.ExportException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex:CustomException): ResponseEntity<ErrorResponseDto> =
        ResponseEntity.status(ex.status()).body(ErrorResponseDto.from(ex))

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleIntegrity(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponseDto> {
        val msg = "필수 필드에 null이 입력되었습니다"
        val detail = ex.mostSpecificCause?.message
        val columnName = detail?.let {
            val regex = Regex("Column '(.*?)' cannot be null")
            regex.find(it)?.groupValues?.getOrNull(1) ?: "알 수 없는 필드"
        } ?: "알 수 없음"
        val errorResponse = ErrorResponseDto(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "$msg..."
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDto> {
        val msg = ex.bindingResult.fieldErrors.firstOrNull()
            ?.let {"${it.field} : ${it.defaultMessage}"}
            ?: ErrorCode.INVALID_INPUT_VALUE.message
        val errorResponse = ErrorResponseDto(
            status = HttpStatus.BAD_REQUEST.value(),
            message = msg
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnknownException(ex:Exception): ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = firstLines(trace(ex), 10)
        )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse)
    }

    fun firstLines(trace: String, lines: Int): String =
        trace.lineSequence().take(lines).joinToString (System.lineSeparator())

    private fun trace(t: Throwable): String =
        StringWriter().also {t.printStackTrace(PrintWriter(it))}.toString()

}