package com.spring.localparking.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Configuration
class JwtDecoderConfig {
    @Bean
    fun appleJwtDecoder(): JwtDecoder =
        NimbusJwtDecoder.withJwkSetUri("https://appleid.apple.com/auth/keys")
            .build()
}
