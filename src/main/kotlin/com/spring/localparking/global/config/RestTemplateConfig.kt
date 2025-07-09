package com.spring.localparking.global.config

import org.springframework.boot.web.client.ClientHttpRequestFactorySettings
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.*
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class RestTemplateConfig {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate =
        builder
            .requestFactory { _: ClientHttpRequestFactorySettings ->
                BufferingClientHttpRequestFactory(HttpComponentsClientHttpRequestFactory())
            }
            .setConnectTimeout(Duration.ofSeconds(3))
            .setReadTimeout(Duration.ofSeconds(5))
            .additionalInterceptors(LoggingClientHttpRequestInterceptor())
            .build()
}

class LoggingClientHttpRequestInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val start = System.currentTimeMillis()
        val resp = execution.execute(request, body)
        val end = System.currentTimeMillis()
        println("[RestTemplate] ${request.method} ${request.uri} => ${resp.statusCode} (${end - start}ms)")
        return resp
    }
}
