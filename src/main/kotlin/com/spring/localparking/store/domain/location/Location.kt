package com.spring.localparking.store.domain.location

import jakarta.persistence.*

@Entity
class Location (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long,

    @Embedded
    @AttributeOverrides(
        value = [
            AttributeOverride(name = "sido",       column = Column(name = "doro_sido")),
            AttributeOverride(name = "sigungu",    column = Column(name = "doro_sigungu")),
            AttributeOverride(name = "doroName",   column = Column(name = "doro_name")),
            AttributeOverride(name = "buildingNo", column = Column(name = "doro_building_no")),
            AttributeOverride(name = "fullAddress",column = Column(name = "doro_full_address"))
        ]
    )
    var doroAddress: DoroAddress? = null,

    @Embedded
    @AttributeOverrides(
        value = [
            AttributeOverride(name = "eupmyeondong", column = Column(name = "jibeon_eupmyeondong")),
            AttributeOverride(name = "bunji",        column = Column(name = "jibeon_bunji")),
            AttributeOverride(name = "fullAddress",  column = Column(name = "jibeon_full_address"))
        ]
    )
    var jibeonAddress: JibeonAddress? = null,

    var lat:Double,
    var lon:Double

)