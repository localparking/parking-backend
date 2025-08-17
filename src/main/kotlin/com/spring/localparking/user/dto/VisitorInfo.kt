package com.spring.localparking.user.dto

import com.spring.localparking.user.domain.UserProfile
import io.swagger.v3.oas.annotations.media.Schema

data class VisitorInfo (
    @Schema(description = "방문자 이름", example = "홍길동")
    val name: String?,
    @Schema(description = "방문자 전화번호", example = "010-1234-5678")
    val tel: String?,
    @Schema(description = "방문자 지역명", example = "서울시 강남구")
    val regionName: String?,
    @Schema(description = "방문자 차량번호", example = "12가 3456")
    val vehicleNumber: String?,
){
    companion object {
        fun from(up : UserProfile) = VisitorInfo(
            name = up.name,
            tel = up.tel,
            regionName = up.regionName,
            vehicleNumber = up.vehicleNumber
        )
    }
}