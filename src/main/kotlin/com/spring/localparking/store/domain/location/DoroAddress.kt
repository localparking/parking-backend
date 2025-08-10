package com.spring.localparking.store.domain.location

import jakarta.persistence.Embeddable

@Embeddable
class DoroAddress (
    var sido: String,
    var sigungu: String,
    var doroName: String,
    var buildingNo: String,
    var fullAddress: String
)