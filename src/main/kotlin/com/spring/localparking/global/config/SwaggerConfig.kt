package com.spring.localparking.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI() : OpenAPI {
        val securityScheme = "Bearer Token"

        val securityRequirement = SecurityRequirement()
            .addList(securityScheme)

        return OpenAPI()
            .info(
                Info()
                    .title("Local Parking API")
                    .version("v1.0.0")
                    .description("Parking API TEST")
            )
            .components(
                Components().addSecuritySchemes(
                    securityScheme,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
            .security(listOf(securityRequirement))
    }
}