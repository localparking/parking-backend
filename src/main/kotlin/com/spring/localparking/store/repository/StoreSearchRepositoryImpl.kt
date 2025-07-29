package com.spring.localparking.store.repository

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
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
        pageable: Pageable,
        searchRadiusKm: Int
    ): Page<StoreDocument> {

        val lat = request.lat ?: 37.498095
        val lon = request.lon ?: 127.027610

        val nativeQuery = NativeQuery.builder()
            .withQuery { q ->
                q.bool { b ->
                    b.applyFilters(request, categoryFilterIds, lat, lon, searchRadiusKm)
                }
            }
            .withPageable(pageable)
            .apply {
                when (request.sort) {
                    SortType.PRICE -> {
                        withSort { s ->
                            s.field { f -> f.field("freeMinutes").order(SortOrder.Asc) }
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

        val hits = operations.search(nativeQuery, StoreDocument::class.java)
        return PageImpl(hits.map { it.content }.toList(), pageable, hits.totalHits)
    }

    override fun searchByText(
        request: StoreSearchRequest,
        categoryFilterIds: List<Long>?,
        pageable: Pageable,
        searchRadiusKm: Int
    ): Page<StoreDocument> {

        val lat = request.lat ?: 37.498095
        val lon = request.lon ?: 127.027610

        val nativeQuery = NativeQuery.builder()
            .withQuery { q ->
                q.bool { b ->
                    b.must { m ->
                        request.query?.takeIf { it.isNotBlank() }?.let {
                            m.multiMatch { mm ->
                                mm.query(it)
                                    .fields(
                                        "name^5",
                                        "categoryNames^4",
                                        "fullDoroAddress^1.5",
                                        "fullJibeonAddress"
                                    )
                                    .type(TextQueryType.BestFields)
                            }
                        }
                    }
                    b.applyFilters(request, categoryFilterIds, lat, lon, searchRadiusKm)
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
            .apply {
                withSort { s -> s.score { it.order(SortOrder.Desc) } }
            }
            .build()

        val searchHits = operations.search(nativeQuery, StoreDocument::class.java)
        val content = searchHits.searchHits.map { it.content }
        return PageImpl(content, pageable, searchHits.totalHits)
    }

    private fun BoolQuery.Builder.applyFilters(
        request: StoreSearchRequest,
        categoryFilterIds: List<Long>?,
        lat: Double,
        lon: Double,
        searchRadiusKm: Int
    ): BoolQuery.Builder {
        val filters = mutableListOf<Query>()
        filters.add(geoDistanceFilter(lat, lon, searchRadiusKm))

        categoryFilterIds?.takeIf { it.isNotEmpty() }?.let { ids ->
            filters.add(termsFilter("categoryIds", ids))
        }

        request.maxFreeMin?.let { mf ->
            filters.add(rangeGte("freeMinutes", mf))
        }

        if (request.is24Hours == true) {
            filters.add(termFilter("is24Hours", true))
        } else {
            val useSpecificCheckTime = !request.checkDayOfWeek.isNullOrBlank() && !request.checkTime.isNullOrBlank()
            if (useSpecificCheckTime) {
                addOperatingHoursFilter(filters, request)
            } else if (request.isOpen != null) {
                filters.add(termFilter("isOpen", request.isOpen))
            }
        }
        return this.filter(filters)
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
        filters.add(isOpenQuery)
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
                            sub.add(Query.of { f -> f.term { t -> t.field("operatingHours.dayOfWeek").value(day.name) } })
                            sub.add(Query.of { f -> f.term { t -> t.field("operatingHours.isOvernight").value(isOvernight) } })

                            beginLte?.let { v ->
                                sub.add(Query.of { f -> f.range { r -> r.field("operatingHours.beginTime").lte(JsonData.of(v)) } })
                            }
                            endGt?.let { v ->
                                sub.add(Query.of { f -> f.range { r -> r.field("operatingHours.endTime").gt(JsonData.of(v)) } })
                            }
                            b.filter(sub)
                        }
                    }
            }
        }
    }
}