package com.spring.localparking.parking.repository

import com.spring.localparking.parking.domain.ParkingLotDocument
import com.spring.localparking.parking.dto.ParkingLotSearchRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface ParkingLotSearchRepositoryCustom {
    fun searchByFilters(request: ParkingLotSearchRequest, pageable: Pageable): Page<ParkingLotDocument>

    fun searchByTextAndLocation(
        query: String,
        lat: Double?,
        lon: Double?,
        distanceKm: Int,
        pageable: Pageable
    ): Page<ParkingLotDocument>
}