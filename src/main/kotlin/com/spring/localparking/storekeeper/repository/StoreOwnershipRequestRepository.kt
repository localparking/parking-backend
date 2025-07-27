package com.spring.localparking.storekeeper.repository

import com.spring.localparking.global.dto.RequestStatus
import com.spring.localparking.storekeeper.dto.StoreOwnershipRequest
import org.springframework.data.jpa.repository.JpaRepository

interface StoreOwnershipRequestRepository : JpaRepository<StoreOwnershipRequest, Long> {
    fun findByStoreIdAndUserIdAndStatus(storeId: Long, userId: Long, status: RequestStatus): StoreOwnershipRequest?
    fun findByStatus(status: RequestStatus): List<StoreOwnershipRequest>
}