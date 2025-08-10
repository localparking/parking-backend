package com.spring.localparking.store.domain.location

import jakarta.persistence.Embeddable

@Embeddable
class JibeonAddress(
    var eupmyeondong: String,
    var bunji: String,
    var fullAddress: String
)