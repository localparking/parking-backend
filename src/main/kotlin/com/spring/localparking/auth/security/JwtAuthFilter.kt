package com.spring.localparking.auth.security

import com.spring.localparking.global.util.JwtUtil
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import io.jsonwebtoken.Claims
import io.jsonwebtoken.io.IOException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.rmi.ServerException

@Component
class JwtAuthFilter(
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository
) : OncePerRequestFilter(){

    @Throws(ServerException::class, IOException::class)
    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        chain: FilterChain
    ) {
        val header = req.getHeader("Authorization")
        if (!header.isNullOrBlank() && header.startsWith("Bearer")) {
            val token = header.substring(7)
            val claims: Claims = jwtUtil.parse(token)
            val uid: Long = claims.subject.toLong()

            val user = userRepository.findById(uid)
                .orElseThrow { UserNotFoundException() }

            val authentication = UsernamePasswordAuthenticationToken(
                user.id,
                null,
                listOf(SimpleGrantedAuthority(user.role.value))
            )

            SecurityContextHolder.getContext().authentication = authentication
        }

        chain.doFilter(req, res)
    }
}