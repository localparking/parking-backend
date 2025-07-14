package com.spring.localparking.api.config

import com.spring.localparking.api.dto.ParkingInfo
import com.spring.localparking.api.dto.SeoulApiDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SeoulParkingApiClient(private val webClient: WebClient) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val baseUrl = "https://data.seoul.go.kr/SeoulRtd"

    fun fetchParkingDataForHotspot(hotspotName: String): List<ParkingInfo> {
        val url = "$baseUrl/parking?hotspotNm=$hotspotName"
        try {
            val response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(SeoulApiDto::class.java)
                .block()
            return response?.privateParkingList ?: emptyList()

        } catch (e: Exception) {
            log.error("API 호출 중 오류 발생: hotspot='{}', url='{}' - {}", hotspotName, url, e.message)
            return emptyList()
        }
    }
}