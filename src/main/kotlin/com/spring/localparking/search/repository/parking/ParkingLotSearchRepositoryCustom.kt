package com.spring.localparking.search.repository.parking

import com.spring.localparking.search.domain.ParkingLotDocument
import com.spring.localparking.search.dto.ParkingLotSearchRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface ParkingLotSearchRepositoryCustom {
    fun searchByFilters(request: ParkingLotSearchRequest,
                        pageable: Pageable,
                        searchRadiusKm: Int): Page<ParkingLotDocument>

    fun searchByText(
        request: ParkingLotSearchRequest,
        pageable: Pageable,
        searchRadiusKm: Int
    ): Page<ParkingLotDocument>
}