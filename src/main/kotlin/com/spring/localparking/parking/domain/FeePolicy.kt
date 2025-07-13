package com.spring.localparking.parking.domain

import jakarta.persistence.*

@Entity
@Table(name = "fee_policy")
class FeePolicy (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "base_fee")
    val baseFee: Int?,

    @Column(name = "base_time_min")
    val baseTimeMin: Int?,

    @Column(name = "additional_fee")
    val additionalFee: Int?,

    @Column(name = "additional_time_min")
    val additionalTimeMin: Int?,

    @Column(name = "day_pass_fee")
    val dayPassFee: Int? = null,

    @Column(name = "mon_pass_fee")
    val monthlyPassFee: Int? = null
)
