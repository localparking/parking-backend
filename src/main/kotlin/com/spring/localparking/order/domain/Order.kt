package com.spring.localparking.order.domain

import com.spring.localparking.global.dto.OrderStatus
import com.spring.localparking.store.domain.Store
import com.spring.localparking.user.domain.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "orders")
class Order (
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id : UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    val store: Store,

    @Column(nullable = false)
    var visitorName: String,

    @Column(nullable = false)
    var visitorTel: String,

    @Column(nullable = false)
    var visitorRegionName: String,

    @Column(nullable = false)
    var visitorVehicleNumber: String,

    @Column(nullable = false)
    var visitTime: LocalDateTime,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var orderItems: MutableList<OrderItem> = mutableListOf(),

    @Column(nullable = false)
    var totalPrice: Int,

    @Column(nullable = false)
    val totalDiscount: Int = 0,

    @Column(nullable = false)
    val parkingFeeDiscount: Int = 0,

    @Column(nullable = false)
    val parkingDiscountMin: Int = 0,

    var paymentKey: String? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var isDeparted: Boolean = false

)