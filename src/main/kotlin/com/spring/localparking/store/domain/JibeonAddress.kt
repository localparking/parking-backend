package com.spring.localparking.store.domain

import jakarta.persistence.Embeddable

@Embeddable
class JibeonAddress(
    var eupmyeondong: String,
    var bunji: String,
    var fullAddress: String
)