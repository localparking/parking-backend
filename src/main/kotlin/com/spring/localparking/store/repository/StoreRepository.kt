package com.spring.localparking.store.repository

import com.spring.localparking.store.domain.Store
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StoreRepository : JpaRepository<Store, Long> {
    @EntityGraph(attributePaths = [
        "storeParkingLots", "storeParkingLots.parkingLot",
        "storeParkingLots.parkingLot.operatingHour", "storeParkingLots.parkingLot.feePolicy",
        "parkingBenefits"
    ])
    fun findWithParkingLotsById(id: Long): Store?
    @EntityGraph(attributePaths = [
        "location",
        "categories.category",
        "operatingHour.timeSlots",
        "storeParkingLots.parkingLot.feePolicy",
        "parkingBenefits"
    ])
    override fun findAll(): List<Store>
    fun findByOwnerId(ownerId: Long): List<Store>

}