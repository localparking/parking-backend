package com.spring.localparking.storekeeper.repository

import com.spring.localparking.storekeeper.domain.StoreParkingBenefit
import org.springframework.data.jpa.repository.JpaRepository

interface StoreParkingBenefitRepository : JpaRepository<StoreParkingBenefit, Long>{
    fun countByStoreId(storeId: Long): Long
}