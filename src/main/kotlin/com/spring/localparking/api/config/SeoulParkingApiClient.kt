package com.spring.localparking.api.config
import com.spring.localparking.api.dto.SeoulApiDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class SeoulParkingApiClient(private val webClient: WebClient) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${seoul-api.key}")
    private lateinit var apiKey: String

    private val baseUrl = "http://openapi.seoul.go.kr:8088"
    private val serviceName = "citydata"

    fun fetchDataForArea(areaName: String): List<SeoulApiDto.ParkingInfo> {
        val results = mutableListOf<SeoulApiDto.ParkingInfo>()
        var startIndex = 1
        val pageSize = 1000

        while (true) {
            val url = "$baseUrl/$apiKey/json/$serviceName/$startIndex/${startIndex + pageSize - 1}/$areaName"

            try {
                val response = webClient.get().uri(url).retrieve().bodyToMono<SeoulApiDto>().block()
                val parkingInfos = response?.cityData?.parkingStatus ?: emptyList()
                if (parkingInfos.isEmpty() || parkingInfos.size < pageSize) {
                    results.addAll(parkingInfos)
                    break
                }

                if (parkingInfos.isEmpty()) {
                    break
                }

                results.addAll(parkingInfos)
                startIndex += pageSize

            } catch (e: Exception) {
                log.error("API 호출 중 오류 발생: area='{}', url='{}' - {}", areaName, url, e.message)
                break
            }
        }
        return results
    }
}