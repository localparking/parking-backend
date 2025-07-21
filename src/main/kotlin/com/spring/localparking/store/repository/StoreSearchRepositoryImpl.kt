package com.spring.localparking.store.repository

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.Query
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
import java.time.LocalDate
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

        val nativeQuery = NativeQuery.builder()
            .withQuery { root ->
                root.bool { bool ->
                    val filters = mutableListOf<Query>()

                    // 2km 고정 거리 필터
                    filters += geoDistanceFilter(request.lat, request.lon)

                    // 카테고리 (이미 root 포함/미포함 결정된 집합)
                    categoryFilterIds?.takeIf { it.isNotEmpty() }?.let { ids ->
                        filters += termsFilter("categoryIds", ids)
                    }

                    // 무료 주차 분 (이상)
                    request.maxFreeMin?.let { mf ->
                        filters += rangeGte("maxFreeMin", mf)
                    }

                    // 동적 시간 필터 사용 여부 판단
                    val useSpecificCheckTime =
                        !request.checkDayOfWeek.isNullOrBlank() && !request.checkTime.isNullOrBlank()

                    // 스냅샷 isOpen (색인필드) 필터: 특정시간 쿼리 안 쓰는 경우에만
                    if (request.isOpen == true && !useSpecificCheckTime) {
                        filters += termFilter("isOpen", true)
                    }

                    // 24시간 필터
                    if (request.is24Hours == true) {
                        filters += termFilter("is24Hours", true)
                    }

                    // 특정 시각 혹은 isOpen+시각 조합 → nested 시간 기반 재계산
                    if (useSpecificCheckTime || (request.isOpen == true && useSpecificCheckTime)) {
                        addOperatingHoursFilter(filters, request, useSpecificCheckTime)
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
                                .location { l -> l.latlon { it.lat(request.lat).lon(request.lon) } }
                                .order(SortOrder.Asc)
                        }
                    }
                }
            }
            .build()

        val hits = operations.search(nativeQuery, StoreDocument::class.java)
        return PageImpl(hits.map { it.content }.toList(), pageable, hits.totalHits)
    }

    // ---------------- helper methods ----------------

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
        request: StoreSearchRequest,
        useSpecificCheckTime: Boolean
    ) {
        val (checkDay, checkTimeInt) = if (useSpecificCheckTime) {
            val day = DayOfWeek.valueOf(request.checkDayOfWeek!!.uppercase())
            val t = LocalTime.parse(request.checkTime!!, DateTimeFormatter.ofPattern("HHmm"))
            day to (t.hour * 100 + t.minute)
        } else {
            val now = LocalDate.now()
            val t = LocalTime.now()
            now.dayOfWeek to (t.hour * 100 + t.minute)
        }

        val yesterday = checkDay.minus(1)

        val isOpenQuery = Query.of { f ->
            f.bool { b ->
                b.should(
                    createOperatingHourQuery(checkDay, false, beginLte = checkTimeInt, endGt = checkTimeInt),
                    createOperatingHourQuery(checkDay, true, beginLte = checkTimeInt),
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
}
