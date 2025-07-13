package com.spring.localparking.parking.repository

import com.spring.localparking.parking.domain.FeePolicy
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeePolicyRepository: JpaRepository<FeePolicy, Long> {
}