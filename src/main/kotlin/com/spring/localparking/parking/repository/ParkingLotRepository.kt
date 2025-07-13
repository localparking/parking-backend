package com.spring.localparking.parking.repository

import com.spring.localparking.parking.domain.ParkingLot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParkingLotRepository : JpaRepository<ParkingLot, String> {
}