package com.spring.localparking.store.domain

import jakarta.persistence.Embeddable

@Embeddable
class DoroAddress (
    var sido: String,
    var sigungu: String,
    var doroName: String,
    var buildingNo: String,
    var fullAddress: String
)