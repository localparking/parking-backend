package com.spring.localparking.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
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
    @Bean
    fun commonApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("common")
            .displayName("일반")
            .pathsToMatch(
                "/store/**",
                "/parking/**",
                "/category/**",
                "/text-search/**"
            )
            .build()
    }
    @Bean
    fun userApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("user")
            .displayName("사용자")
            .pathsToMatch(
                "/user/**",
                "/auth/**",
                "/register/**",
                "/onboarding/**",
                "/order/**"
            )
            .build()
    }
    @Bean
    fun storekeeperApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("storekeeper")
            .displayName("점주")
            .pathsToMatch(
                "/storekeeper/**"
            )
            .build()
    }

    @Bean
    fun adminApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("admin")
            .displayName("관리자")
            .pathsToMatch(
                "/admin/**"
            )
            .build()
    }
}