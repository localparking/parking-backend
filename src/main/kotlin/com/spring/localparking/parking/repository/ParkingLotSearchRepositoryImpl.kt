package com.spring.localparking.parking.repository

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.json.JsonData
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.spring.localparking.global.dto.SortType
import com.spring.localparking.parking.domain.ParkingLotDocument
import com.spring.localparking.parking.dto.ParkingLotSearchRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Repository
class ParkingLotSearchRepositoryImpl(
    private val elasticsearchOperations: ElasticsearchOperations
) : ParkingLotSearchRepositoryCustom {

    override fun searchByFilters(request: ParkingLotSearchRequest, pageable: Pageable): Page<ParkingLotDocument> {

        val nativeQuery = NativeQuery.builder()
            .withQuery { q ->
                q.bool { b ->
                    val filters = mutableListOf<Query>()

                    // Location filter
                    filters.add(Query.of { t ->
                        t.geoDistance { g ->
                            g
                                .field("location")
                                .distance("2km") // 2km 반경 설정
                                .location { l -> l.latlon { ll -> ll.lat(request.lat).lon(request.lon) } }
                        }
                    })

                    // Boolean filters
                    if (request.isFree == true) {
                        filters.add(Query.of { t -> t.term { it.field("isFree").value(true) } })
                    }
                    if (request.isRealtime == true) {
                        filters.add(Query.of { t -> t.term { it.field("isRealtime").value(true) } })
                    }

                    // Range filter for hourly fee
                    request.maxFeePerHour?.let { fee ->
                        filters.add(Query.of { t ->
                            t.range {
                                // FIXED: Use JsonData.of() for range queries
                                it.field("hourlyFee").lte(JsonData.of(fee))
                            }
                        })
                    }
                    // 24-hour filter
                    if (request.is24Hours == true) {
                        filters.add(Query.of { t -> t.term { it.field("is24Hours").value(true) } })
                    }
                    // Congestion filter
                    request.congestion?.let { congestions ->
                        if (congestions.isNotEmpty()) {
                            filters.add(Query.of { t -> t.terms {
                                it.field("congestion").terms { v -> v
                                    .value(congestions.map { FieldValue.of(it) })
                                }
                            }})
                        }
                    }

                    // "Is Open Now" filter
                    if (request.isOpen == true) {
                        val today = LocalDate.now().dayOfWeek.name
                        val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmm")).toInt()
                        addOperatingHoursFilter(filters, today, currentTime)
                    }

                    // 특정 시간대 운영 여부 필터
                    if (!request.checkDayOfWeek.isNullOrBlank() && !request.checkTime.isNullOrBlank()) {
                        request.checkTime.toIntOrNull()?.let { time ->
                            addOperatingHoursFilter(filters, request.checkDayOfWeek, time)
                        }
                    }

                    b.filter(filters)
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
                            s.field { f ->
                                f.field("hourlyFee").order(SortOrder.Asc)
                            }
                        }
                    }
                }
            }
            .build()

        val searchHits = elasticsearchOperations.search(nativeQuery, ParkingLotDocument::class.java)

        val content = searchHits.searchHits.map { it.content }
        return PageImpl(content, pageable, searchHits.totalHits)
    }

    private fun addOperatingHoursFilter(filters: MutableList<Query>, dayOfWeek: String, time: Int) {
        filters.add(
            Query.of { t ->
                t.nested { n ->
                    n.path("operatingHours")
                        .query { nq ->
                            nq.bool { nb ->
                                nb.must {
                                    it.term { m ->
                                        m.field("operatingHours.dayOfWeek").value(dayOfWeek.uppercase())
                                    }
                                }
                                nb.should(
                                    // Case A: Not overnight (e.g., 09:00 ~ 18:00)
                                    Query.of { s ->
                                        s.bool { sb ->
                                            sb.must(Query.of { m ->
                                                m.term {
                                                    it.field("operatingHours.isOvernight").value(false)
                                                }
                                            })
                                            // JsonData.of()는 Int 타입을 그대로 처리할 수 있음
                                            sb.must(Query.of { m ->
                                                m.range {
                                                    it.field("operatingHours.beginTime").lte(JsonData.of(time))
                                                }
                                            })
                                            sb.must(Query.of { m ->
                                                m.range {
                                                    it.field("operatingHours.endTime").gt(JsonData.of(time))
                                                }
                                            })
                                        }
                                    },
                                    // Case B: Overnight (e.g., 22:00 ~ 06:00)
                                    Query.of { s ->
                                        s.bool { sb ->
                                            sb.must(Query.of { m ->
                                                m.term {
                                                    it.field("operatingHours.isOvernight").value(true)
                                                }
                                            })
                                            sb.should(
                                                Query.of { sh ->
                                                    sh.range {
                                                        it.field("operatingHours.beginTime").lte(JsonData.of(time))
                                                    }
                                                },
                                                Query.of { sh ->
                                                    sh.range {
                                                        it.field("operatingHours.endTime").gt(JsonData.of(time))
                                                    }
                                                }
                                            )
                                            sb.minimumShouldMatch("1")
                                        }
                                    }
                                )
                                nb.minimumShouldMatch("1")
                            }
                        }
                }
            }
        )
    }
}