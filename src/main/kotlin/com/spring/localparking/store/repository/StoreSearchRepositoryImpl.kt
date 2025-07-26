package com.spring.localparking.store.repository

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import co.elastic.clients.json.JsonData
import com.spring.localparking.global.dto.SortType
import com.spring.localparking.store.domain.StoreDocument
import com.spring.localparking.store.dto.StoreSearchRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Repository
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Repository
class StoreSearchRepositoryImpl(
    private val operations: ElasticsearchOperations
) : StoreSearchRepositoryCustom {

    override fun searchByFilters(
        request: StoreSearchRequest,
        categoryFilterIds: List<Long>?,
        pageable: Pageable
    ): Page<StoreDocument> {
        val lat = request.lat ?: 37.498095
        val lon = request.lon ?: 127.027610
        val nativeQuery = NativeQuery.builder()
            .withQuery { root ->
                root.bool { bool ->
                    val filters = mutableListOf<Query>()

                    // 1. 기본 필터 추가
                    filters += geoDistanceFilter(lat, lon)

                    categoryFilterIds?.takeIf { it.isNotEmpty() }?.let { ids ->
                        filters += termsFilter("categoryIds", ids)
                    }
                    request.maxFreeMin?.let { mf ->
                        filters += rangeGte("maxFreeMin", mf)
                    }
                    if (request.is24Hours == true) {
                        filters += termFilter("is24Hours", true)
                    }

                    // 2. 시간 관련 필터 로직 수정
                    val useSpecificCheckTime =
                        !request.checkDayOfWeek.isNullOrBlank() && !request.checkTime.isNullOrBlank()

                    // 특정 시간 필터가 있으면, 항상 해당 시간 기준으로 필터링
                    if (useSpecificCheckTime) {
                        addOperatingHoursFilter(filters, request)
                    }
                    // 특정 시간 필터가 없고, isOpen 필터만 있을 경우
                    else if (request.isOpen != null) {
                        filters += termFilter("isOpen", request.isOpen)
                    }

                    bool.filter(filters)
                }
            }
            .withPageable(pageable)
            .apply {
                if (request.sort == SortType.DISTANCE) {
                    withSort { s ->
                        s.geoDistance { g ->
                            g.field("location")
                                .location { l -> l.latlon { it.lat(lat).lon(lon) } }
                                .order(SortOrder.Asc)
                        }
                    }
                }
            }
            .build()

        val hits = operations.search(nativeQuery, StoreDocument::class.java)
        return PageImpl(hits.map { it.content }.toList(), pageable, hits.totalHits)
    }

    private fun geoDistanceFilter(lat: Double, lon: Double) =
        Query.of { q ->
            q.geoDistance { g ->
                g.field("location").distance("2km")
                    .location { l -> l.latlon { it.lat(lat).lon(lon) } }
            }
        }

    private fun termFilter(field: String, value: Boolean) =
        Query.of { q -> q.term { t -> t.field(field).value(value) } }

    private fun termsFilter(field: String, values: List<Long>) =
        Query.of { q ->
            q.terms { t ->
                t.field(field).terms { v ->
                    v.value(values.map { FieldValue.of(it) })
                }
            }
        }

    private fun rangeGte(field: String, gte: Int) =
        Query.of { q ->
            q.range { r -> r.field(field).gte(JsonData.of(gte)) }
        }

    private fun addOperatingHoursFilter(
        filters: MutableList<Query>,
        request: StoreSearchRequest
    ) {
        // 이 함수는 checkDayOfWeek와 checkTime이 유효할 때만 호출됨
        val day = DayOfWeek.valueOf(request.checkDayOfWeek!!.uppercase())
        val t = LocalTime.parse(request.checkTime!!, DateTimeFormatter.ofPattern("HHmm"))
        val checkTimeInt = t.hour * 100 + t.minute

        val yesterday = day.minus(1)

        val isOpenQuery = Query.of { f ->
            f.bool { b ->
                b.should(
                    createOperatingHourQuery(day, false, beginLte = checkTimeInt, endGt = checkTimeInt),
                    createOperatingHourQuery(day, true, beginLte = checkTimeInt),
                    createOperatingHourQuery(yesterday, true, endGt = checkTimeInt)
                ).minimumShouldMatch("1")
            }
        }
        filters += isOpenQuery
    }

    private fun createOperatingHourQuery(
        day: DayOfWeek,
        isOvernight: Boolean,
        beginLte: Int? = null,
        endGt: Int? = null
    ): Query {
        return Query.of { q ->
            q.nested { n ->
                n.path("operatingHours")
                    .query { nq ->
                        nq.bool { b ->
                            val sub = mutableListOf<Query>()
                            sub += Query.of { f -> f.term { t -> t.field("operatingHours.dayOfWeek").value(day.name) } }
                            sub += Query.of { f -> f.term { t -> t.field("operatingHours.isOvernight").value(isOvernight) } }

                            beginLte?.let { v ->
                                sub += Query.of { f -> f.range { r -> r.field("operatingHours.beginTime").lte(JsonData.of(v)) } }
                            }
                            endGt?.let { v ->
                                sub += Query.of { f -> f.range { r -> r.field("operatingHours.endTime").gt(JsonData.of(v)) } }
                            }
                            b.filter(sub)
                        }
                    }
            }
        }
    }
    override fun searchByTextAndLocation(
        query: String,
        lat: Double?,
        lon: Double?,
        distanceKm: Int,
        pageable: Pageable
    ): Page<StoreDocument> {
        val filterQueries = mutableListOf<Query>()
        if (lat != null && lon != null) {
            val geoDistanceQuery = Query.of { f ->
                f.geoDistance { gd ->
                    gd.field("location")
                        .distance("${distanceKm}km")
                        .location { loc -> loc.latlon { ll -> ll.lat(lat).lon(lon) } }
                }
            }
            filterQueries.add(geoDistanceQuery)
        }

        val nativeQuery = NativeQuery.builder()
            .withQuery { q ->
                q.bool { b ->
                    b.must { m ->
                        m.multiMatch { mm ->
                            mm.query(query)
                                .fields(
                                    "name^3",
                                    "categoryNames^2",
                                    "fullDoroAddress^1.5",
                                    "fullJibeonAddress"
                                )
                                .type(TextQueryType.BestFields)
                        }
                    }
                }
            }
            .withPageable(pageable)
            .apply {
                withSort { s -> s.score { it.order(SortOrder.Desc) } }
            }
            .build()

        val searchHits = operations.search(nativeQuery, StoreDocument::class.java)
        val content = searchHits.searchHits.map { it.content }
        val total = searchHits.totalHits
        return PageImpl(content, pageable, total)
    }
}