package com.spring.localparking.global.config.db

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered                     // ★ 우선순위 지정에 필요
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class DbRoutingInterceptor : HandlerInterceptor {

    override fun preHandle(
        req: HttpServletRequest,
        res: HttpServletResponse,
        handler: Any
    ): Boolean {
        val key = if (req.requestURI.startsWith("/dev")) DbKey.DEV else DbKey.PROD
        DbContextHolder.set(key)
        return true
    }

    override fun afterCompletion(
        req: HttpServletRequest,
        res: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        DbContextHolder.clear()
    }
}

@Configuration
class WebMvcConfig : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(DbRoutingInterceptor())
            .order(Ordered.HIGHEST_PRECEDENCE)
    }
}
