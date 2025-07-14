package com.spring.localparking.parking.repository

import com.spring.localparking.parking.domain.ParkingLotDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

@Repository
interface ParkingLotSearchRepository : ElasticsearchRepository<ParkingLotDocument, String>, ParkingLotSearchRepositoryCustom {
}