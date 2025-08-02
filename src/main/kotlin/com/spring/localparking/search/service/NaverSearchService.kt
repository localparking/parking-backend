package com.spring.localparking.search.service

import com.spring.localparking.search.dto.NaverApiResponse
import com.spring.localparking.search.dto.SearchItemResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class NaverSearchService(
    private val webClient: WebClient.Builder
) {

    @Value("\${naver.api.client-id}")
    private lateinit var clientId: String

    @Value("\${naver.api.client-secret}")
    private lateinit var clientSecret: String

    fun search(query: String): List<SearchItemResponse> {
        val uri: URI = UriComponentsBuilder
            .fromUriString("https://openapi.naver.com")
            .path("/v1/search/local.json")
            .queryParam("query", query)
            .queryParam("display", 5)
            .queryParam("start", 1)
            .queryParam("sort", "random")
            .encode()
            .build()
            .toUri()

        val naverResponse: NaverApiResponse? = webClient.build()
            .get()
            .uri(uri)
            .header("X-Naver-Client-Id", clientId)
            .header("X-Naver-Client-Secret", clientSecret)
            .retrieve()
            .bodyToMono(NaverApiResponse::class.java) // 👈 이 부분 수정
            .block()

        return naverResponse?.items?.map { naverItem ->
            SearchItemResponse(
                title = naverItem.title,
                roadAddress = naverItem.roadAddress,
                lat = naverItem.mapy.toDouble(),
                lon = naverItem.mapx.toDouble()
            )
        } ?: emptyList()
    }
}