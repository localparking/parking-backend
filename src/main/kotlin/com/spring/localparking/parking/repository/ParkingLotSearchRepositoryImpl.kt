package com.spring.localparking.parking.repository

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.json.JsonData
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.spring.localparking.global.dto.SortType
import com.spring.localparking.parking.domain.ParkingLotDocument
import com.spring.localparking.parking.dto.ParkingLotSearchRequest
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Repository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Repository
@Lazy
class ParkingLotSearchRepositoryImpl(
    private val elasticsearchOperations: ElasticsearchOperations
) : ParkingLotSearchRepositoryCustom {

    override fun searchByFilters(request: ParkingLotSearchRequest, pageable: Pageable): Page<ParkingLotDocument> {
        val nativeQuery = NativeQuery.builder()
            .withQuery { q ->
                q.bool { b ->
                    val allFilters = mutableListOf<Query>()
                    addBaseFilters(allFilters, request)

                    val useSpecificCheckTime = !request.checkDayOfWeek.isNullOrBlank() && !request.checkTime.isNullOrBlank()
                    if (request.isOpen == true || useSpecificCheckTime) {
                        addOperatingHoursFilter(allFilters, request, useSpecificCheckTime)
                    }

                    b.filter(allFilters)
                }
            }
            .withPageable(pageable)
            .apply {
                when (request.sort) {
                    SortType.DISTANCE -> {
                        withSort { s ->
                            s.geoDistance { g ->
                                g.field("location")
                                    .location { l -> l.latlon { it.lat(request.lat).lon(request.lon) } }
                                    .order(SortOrder.Asc)
                            }
                        }
                    }
                    SortType.PRICE -> {
                        withSort { s ->
                            s.field { f -> f.field("hourlyFee").order(SortOrder.Asc) }
                        }
                    }
                }
            }
            .build()

        val searchHits = elasticsearchOperations.search(nativeQuery, ParkingLotDocument::class.java)
        return PageImpl(searchHits.map { it.content }.toList(), pageable, searchHits.totalHits)
    }

    private fun addBaseFilters(filters: MutableList<Query>, request: ParkingLotSearchRequest) {
        filters.add(Query.of { t ->
            t.geoDistance { g ->
                g.field("location").distance("2km")
                    .location { l -> l.latlon { ll -> ll.lat(request.lat).lon(request.lon) } }
            }
        })
        if (request.isFree == true) {
            filters.add(Query.of { t -> t.term { it.field("isFree").value(true) } })
        }
        if (request.isRealtime == true) {
            filters.add(Query.of { t -> t.term { it.field("isRealtime").value(true) } })
        }
        if (request.is24Hours == true) {
            filters.add(Query.of { t -> t.term { it.field("is24Hours").value(true) } })
        }
        request.maxFeePerHour?.let { fee ->
            filters.add(Query.of { t -> t.range { it.field("hourlyFee").lte(JsonData.of(fee)) } })
        }
        request.congestion?.let { congestions ->
            if (congestions.isNotEmpty()) {
                filters.add(Query.of { t -> t.terms { it.field("congestion").terms { v -> v.value(congestions.map { FieldValue.of(it) }) } } })
            }
        }
    }

    private fun addOperatingHoursFilter(filters: MutableList<Query>, request: ParkingLotSearchRequest, useSpecificCheckTime: Boolean) {
        val (checkDay, checkTimeInt) = if (useSpecificCheckTime) {
            val day = DayOfWeek.valueOf(request.checkDayOfWeek!!.uppercase())
            val time = LocalTime.parse(request.checkTime!!, DateTimeFormatter.ofPattern("HHmm"))
            day to (time.hour * 100 + time.minute)
        } else {
            val now = LocalDate.now()
            val time = LocalTime.now()
            now.dayOfWeek to (time.hour * 100 + time.minute)
        }

        val yesterday = checkDay.minus(1)

        val isOpenQuery = Query.of { f ->
            f.bool { bool ->
                bool.should(
                    createOperatingHourQuery(checkDay, isOvernight = false, beginLte = checkTimeInt, endGt = checkTimeInt),
                    createOperatingHourQuery(checkDay, isOvernight = true, beginLte = checkTimeInt),
                    createOperatingHourQuery(yesterday, isOvernight = true, endGt = checkTimeInt)
                ).minimumShouldMatch("1")
            }
        }
        filters.add(isOpenQuery)
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