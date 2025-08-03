package com.spring.localparking.admin.dto

import com.spring.localparking.user.domain.User

data class StorekeeperRequestResponse (
    val userId: Long,
    val adminId: String?,
    val email: String,
    val storeName: String?,
    val businessNumber: String?,
    val storeAddress: String?
){
    companion object {
        fun from(user: User, store: com.spring.localparking.store.domain.Store?): StorekeeperRequestResponse {
            return StorekeeperRequestResponse(
                userId = user.id!!,
                adminId = user.adminId,
                email = user.email,
                storeName = store?.name,
                businessNumber = store?.businessNumber,
                storeAddress = store?.location?.doroAddress?.fullAddress
            )
        }
    }
}