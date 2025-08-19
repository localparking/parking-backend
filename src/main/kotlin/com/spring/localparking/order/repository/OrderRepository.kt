package com.spring.localparking.order.repository

import com.spring.localparking.order.domain.Order
import com.spring.localparking.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findTopByUserAndIsDepartedFalseOrderByCreatedAtDesc(user: User): Order?
}