package com.spring.localparking.search.repository.parking

import com.spring.localparking.search.domain.ParkingLotDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

@Repository
interface ParkingLotSearchRepository : ElasticsearchRepository<ParkingLotDocument, String>,
    ParkingLotSearchRepositoryCustom {
}