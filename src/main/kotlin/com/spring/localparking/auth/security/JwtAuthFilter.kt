package com.spring.localparking.auth.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.spring.global.exception.ErrorCode
import com.spring.localparking.global.util.JwtUtil
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtUtil: JwtUtil,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        chain: FilterChain
    ) {
        val header = req.getHeader("Authorization")
        if (header.isNullOrBlank() || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res)
            return
        }

        try {
            val token = header.substring(7)
            val claims = jwtUtil.parse(token)
            val uid = claims.subject.toLong()

            val user = userRepository.findById(uid)
                .orElseThrow { UserNotFoundException() }
            val principal = CustomPrincipal(user)

            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.authorities
            )
            SecurityContextHolder.getContext().authentication = authentication

        } catch (e: Exception) {
            val errorCode = when (e) {
                is UserNotFoundException -> ErrorCode.USER_NOT_FOUND
                is SignatureException, is MalformedJwtException, is UnsupportedJwtException -> ErrorCode.INVALID_TOKEN
                is ExpiredJwtException -> ErrorCode.TOKEN_EXPIRED
                else -> ErrorCode.UNAUTHORIZED
            }
            setErrorResponse(res, errorCode)
            return
        }

        chain.doFilter(req, res)
    }

    private fun setErrorResponse(response: HttpServletResponse, errorCode: ErrorCode) {
        response.contentType = "application/json;charset=UTF-8"
        response.status = errorCode.status.value()
        val errorResponse = mapOf(
            "status" to errorCode.status.value(),
            "message" to errorCode.message
        )
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}