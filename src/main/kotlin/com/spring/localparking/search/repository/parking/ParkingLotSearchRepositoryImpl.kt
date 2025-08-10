package com.spring.localparking.search.repository.parking

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import co.elastic.clients.json.JsonData
import com.spring.localparking.global.dto.SortType
import com.spring.localparking.search.domain.ParkingLotDocument
import com.spring.localparking.search.dto.ParkingLotSearchRequest
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Repository
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Repository
@Lazy
class ParkingLotSearchRepositoryImpl(
    private val elasticsearchOperations: ElasticsearchOperations
) : ParkingLotSearchRepositoryCustom {
    override fun searchByName(query: String): List<ParkingLotDocument> {
        val nativeQuery = NativeQuery.builder()
            .withQuery { q ->
                q.wildcard { w ->
                    w.field("name.keyword")
                        .value("*$query*")
                        .caseInsensitive(true)
                }
            }
            .withPageable(PageRequest.of(0, 20))
            .build()

        val hits = elasticsearchOperations.search(nativeQuery, ParkingLotDocument::class.java)
        return hits.map { it.content }.toList()
    }

    /**
     * 지도 기반 필터 검색
     */
    override fun searchByFilters(
        request: ParkingLotSearchRequest,
        pageable: Pageable,
        searchRadiusKm: Int
    ): Page<ParkingLotDocument> {
        val lat = request.lat ?: 37.498095
        val lon = request.lon ?: 127.027610

        val nativeQuery = NativeQuery.builder()
            .withQuery { q ->
                q.bool { b ->
                    b.applyFilters(request, lat, lon, searchRadiusKm)
                }
            }
            .withPageable(pageable)
            .apply {
                when (request.sort) {
                    SortType.PRICE -> {
                        withSort { s ->
                            s.field { f -> f.field("hourlyFee").order(SortOrder.Asc) }
                        }
                    }
                    else -> {
                        withSort { s ->
                            s.geoDistance { g ->
                                g.field("location")
                                    .location { l -> l.latlon { it.lat(lat).lon(lon) } }
                                    .order(SortOrder.Asc) }}
                    }
                }
            }
            .build()

        val searchHits = elasticsearchOperations.search(nativeQuery, ParkingLotDocument::class.java)
        return PageImpl(searchHits.map { it.content }.toList(), pageable, searchHits.totalHits)
    }

    /**
     * 텍스트 기반 필터 검색
     */
    override fun searchByText(
        request: ParkingLotSearchRequest,
        pageable: Pageable,
        searchRadiusKm: Int): Page<ParkingLotDocument> {
        val lat = request.lat ?: 37.498095
        val lon = request.lon ?: 127.027610

        val nativeQuery = NativeQuery.builder()
            .withQuery { q ->
                q.bool { b ->
                    request.query?.takeIf { it.isNotBlank() }?.let {
                        b.must { m ->
                            m.multiMatch { mm ->
                                mm.query(it)
                                    .fields("name^3", "address^1.5")
                                    .type(TextQueryType.BestFields)
                            }
                        }
                    }
                    b.applyFilters(request, lat, lon, searchRadiusKm)
                }
            }
            .withPageable(pageable)
            .apply {
                when (request.sort) {
                    SortType.DISTANCE -> {
                        withSort { s ->
                            s.geoDistance { g ->
                                g.field("location")
                                    .location { l -> l.latlon { it.lat(lat).lon(lon) } }
                                    .order(SortOrder.Asc)
                            }
                        }
                    }
                    SortType.PRICE -> {
                        withSort { s ->
                            s.field { f -> f.field("hourlyFee").order(SortOrder.Asc) }
                        }
                    }
                    else -> {
                        withSort { s -> s.score { it.order(SortOrder.Desc) } }
                    }
                }
            }
            .build()

        val searchHits = elasticsearchOperations.search(nativeQuery, ParkingLotDocument::class.java)
        val content = searchHits.searchHits.map { it.content }
        return PageImpl(content, pageable, searchHits.totalHits)
    }

    /**
     * 공통 필터 적용 확장 함수
     */
    private fun BoolQuery.Builder.applyFilters(
        request: ParkingLotSearchRequest,
        lat: Double,
        lon: Double,
        searchRadiusKm: Int
    ): BoolQuery.Builder {
        val allFilters = mutableListOf<Query>()

        // 기본 필터
        allFilters.add(geoDistanceFilter(lat, lon, searchRadiusKm))
        if (request.isFree == true) {
            allFilters.add(termFilter("isFree", true))
        }
        if (request.isRealtime == true) {
            allFilters.add(termFilter("isRealtime", true))
        }
        request.maxFeePerHour?.let { fee ->
            allFilters.add(rangeLte("hourlyFee", fee))
        }
        request.congestion?.let { congestions ->
            if (congestions.isNotEmpty()) {
                allFilters.add(termsFilter("congestion", congestions))
            }
        }

        // 운영 시간 관련 필터
        addOperatingConditionFilters(allFilters, request)

        return this.filter(allFilters)
    }

    private fun geoDistanceFilter(lat: Double, lon: Double, searchRadiusKm: Int) =
        Query.of { q ->
            q.geoDistance { g ->
                g.field("location").distance("${searchRadiusKm}km")
                    .location { l -> l.latlon { it.lat(lat).lon(lon) } }
            }
        }

    private fun termFilter(field: String, value: Boolean) =
        Query.of { q -> q.term { t -> t.field(field).value(value) } }

    private fun termsFilter(field: String, values: List<String>) =
        Query.of { q ->
            q.terms { t ->
                t.field(field).terms { v ->
                    v.value(values.map { FieldValue.of(it) })
                }
            }
        }

    private fun rangeLte(field: String, lte: Int) =
        Query.of { q ->
            q.range { r -> r.field(field).lte(JsonData.of(lte)) }
        }

    private fun addOperatingConditionFilters(filters: MutableList<Query>, request: ParkingLotSearchRequest) {
        val useSpecificCheckTime = !request.checkDayOfWeek.isNullOrBlank() && !request.checkTime.isNullOrBlank()
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))

        if (request.is24Hours == true) {
            val dayToCheck24h = if (useSpecificCheckTime) DayOfWeek.valueOf(request.checkDayOfWeek!!.uppercase()) else now.dayOfWeek
            filters.add(create24hQuery(dayToCheck24h))
            return
        }

        if (useSpecificCheckTime) {
            filters.add(createDynamicOpenQuery(request.checkDayOfWeek, request.checkTime))
            return
        }

        if (request.isOpen != null) {
            val dayOfWeekStr = now.dayOfWeek.toString()
            val timeStr = now.format(DateTimeFormatter.ofPattern("HHmm"))
            val dynamicOpenQuery = createDynamicOpenQuery(dayOfWeekStr, timeStr)

            if (request.isOpen == true) {
                filters.add(dynamicOpenQuery)
            } else {
                filters.add(Query.of { q -> q.bool { b -> b.mustNot(dynamicOpenQuery) } })
            }
        }
    }

    private fun createDynamicOpenQuery(dayOfWeekStr: String?, timeStr: String?): Query {
        val day = DayOfWeek.valueOf(dayOfWeekStr!!.uppercase())
        val time = LocalTime.parse(timeStr!!, DateTimeFormatter.ofPattern("HHmm"))
        val checkTimeInt = time.hour * 100 + time.minute
        val yesterday = day.minus(1)

        return Query.of { f ->
            f.bool { bool ->
                bool.should(
                    createOperatingHourQuery(day, isOvernight = false, beginLte = checkTimeInt, endGt = checkTimeInt),
                    createOperatingHourQuery(day, isOvernight = true, beginLte = checkTimeInt),
                    createOperatingHourQuery(yesterday, isOvernight = true, endGt = checkTimeInt)
                ).minimumShouldMatch("1")
            }
        }
    }

    private fun create24hQuery(day: DayOfWeek): Query {
        return Query.of { q ->
            q.nested { n ->
                n.path("operatingHours")
                    .query { nq ->
                        nq.bool { b ->
                            val subFilters = mutableListOf<Query>()
                            subFilters.add(Query.of { f -> f.term { t -> t.field("operatingHours.dayOfWeek").value(day.name) } })
                            subFilters.add(Query.of { f -> f.term { t -> t.field("operatingHours.beginTime").value(0) } })
                            subFilters.add(Query.of { f -> f.bool { sb ->
                                sb.should(
                                    Query.of { t -> t.term { it.field("operatingHours.endTime").value(2400) } },
                                    Query.of { t -> t.term { it.field("operatingHours.endTime").value(2359) } }
                                ).minimumShouldMatch("1")
                            }})
                            b.filter(subFilters)
                        }
                    }
            }
        }
    }

    private fun createOperatingHourQuery(day: DayOfWeek, isOvernight: Boolean, beginLte: Int? = null, endGt: Int? = null): Query {
        return Query.of { q ->
            q.nested { n ->
                n.path("operatingHours")
                    .query { nq ->
                        nq.bool { b ->
                            val subFilters = mutableListOf<Query>()
                            subFilters.add(Query.of { f -> f.term { t -> t.field("operatingHours.dayOfWeek").value(day.name) } })
                            subFilters.add(Query.of { f -> f.term { t -> t.field("operatingHours.isOvernight").value(isOvernight) } })

                            beginLte?.let { subFilters.add(Query.of { f -> f.range { r -> r.field("operatingHours.beginTime").lte(JsonData.of(it)) } }) }
                            endGt?.let { subFilters.add(Query.of { f -> f.range { r -> r.field("operatingHours.endTime").gt(JsonData.of(it)) } }) }

                            b.filter(subFilters)
                        }
                    }
            }
        }
    }
}