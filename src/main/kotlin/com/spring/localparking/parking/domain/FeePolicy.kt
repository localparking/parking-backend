package com.spring.localparking.parking.domain

import jakarta.persistence.*

@Entity
@Table(name = "fee_policy")
class FeePolicy (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "base_fee")
    var baseFee: Int,

    @Column(name = "base_time_min")
    var baseTimeMin: Int,

    @Column(name = "additional_fee")
    var additionalFee: Int?,

    @Column(name = "additional_time_min")
    var additionalTimeMin: Int?,

    @Column(name = "day_pass_fee")
    var dayPassFee: Int? = null,

    @Column(name = "mon_pass_fee")
    var monthlyPassFee: Int? = null
)