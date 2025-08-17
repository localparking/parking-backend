
package com.spring.localparking.user.domain

import jakarta.persistence.*

@Entity
class UserProfile(
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    var user: User
) {
    @Id
    val userId: Long? = null

    var name: String? = null
    var tel: String? = null
    var regionName: String? = null
    var vehicleNumber: String? = null

    fun update(
        name: String?,
        tel: String?,
        regionName: String?,
        vehicleNumber: String?
    ) {
        this.name = name?: this.name
        this.tel = tel?: this.tel
        this.regionName = regionName?: this.regionName
        this.vehicleNumber = vehicleNumber?: this.vehicleNumber
    }

}