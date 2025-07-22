package com.spring.localparking.parking.repository

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.json.JsonData
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
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
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
                    addOperatingConditionFilters(allFilters, request) // Unified and corrected logic
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
        request.maxFeePerHour?.let { fee ->
            filters.add(Query.of { t -> t.range { it.field("hourlyFee").lte(JsonData.of(fee)) } })
        }
        request.congestion?.let { congestions ->
            if (congestions.isNotEmpty()) {
                filters.add(Query.of { t -> t.terms { it.field("congestion").terms { v -> v.value(congestions.map { FieldValue.of(it) }) } } })
            }
        }
    }

    private fun addOperatingConditionFilters(filters: MutableList<Query>, request: ParkingLotSearchRequest) {
        val useSpecificCheckTime = !request.checkDayOfWeek.isNullOrBlank() && !request.checkTime.isNullOrBlank()
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))

        // **우선순위 1: is24Hours: true 인 경우**
        if (request.is24Hours == true) {
            val dayToCheck24h = if (useSpecificCheckTime) {
                DayOfWeek.valueOf(request.checkDayOfWeek!!.uppercase())
            } else {
                now.dayOfWeek
            }
            // nested 쿼리만으로 operatingHours의 존재 여부와 조건 매칭을 모두 처리
            val is24hQuery = create24hQuery(dayToCheck24h)
            filters.add(is24hQuery)
            return // 24시간 필터가 가장 우선순위가 높으므로 여기서 종료
        }

        // **우선순위 2: 특정 시간으로 검색하는 경우**
        if (useSpecificCheckTime) {
            val dynamicOpenQuery = createDynamicOpenQuery(request.checkDayOfWeek, request.checkTime)
            filters.add(dynamicOpenQuery)
            return
        }

        // **우선순위 3: 현재 시간 기준으로 영업 여부를 검색하는 경우**
        if (request.isOpen != null) {
            val dayOfWeekStr = now.dayOfWeek.toString()
            val timeStr = now.format(DateTimeFormatter.ofPattern("HHmm"))
            val dynamicOpenQuery = createDynamicOpenQuery(dayOfWeekStr, timeStr)

            if (request.isOpen == true) {
                filters.add(dynamicOpenQuery)
            } else { // isOpen == false
                // 현재 영업 중이 아닌 모든 주차장 (운영시간 정보가 없거나, 현재 닫혀있는 경우)
                filters.add(Query.of { b -> b.bool { it.mustNot(dynamicOpenQuery) } })
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
        // 00:00 ~ 24:00 (데이터상 2359 또는 2400) 슬롯을 찾는 쿼리
        return Query.of { q ->
            q.nested { n ->
                n.path("operatingHours")
                    .query { nq ->
                        nq.bool { b ->
                            val subFilters = mutableListOf<Query>()
                            subFilters.add(Query.of { f -> f.term { t -> t.field("operatingHours.dayOfWeek").value(day.name) } })
                            subFilters.add(Query.of { f -> f.term { t -> t.field("operatingHours.beginTime").value(0) } })
                            subFilters.add(Query.of { f -> f.bool { sb -> sb.should(
                                Query.of { t -> t.term { it.field("operatingHours.endTime").value(2400) } },
                                Query.of { t -> t.term { it.field("operatingHours.endTime").value(2359) } }
                            ).minimumShouldMatch("1") }})
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