package com.spring.localparking.admin.dto

import com.spring.localparking.store.domain.Store
import com.spring.localparking.user.domain.User

data class StorekeeperRequestResponse (
    val adminId: String?,
    val email: String,
    val storeName: String?,
    val businessNumber: String?,
    val storeAddress: String?
){
    companion object {
        fun from(user: User, store: Store?): StorekeeperRequestResponse {
            return StorekeeperRequestResponse(
                adminId = user.adminId,
                email = user.email,
                storeName = store?.name,
                businessNumber = store?.businessNumber,
                storeAddress = store?.location?.doroAddress?.fullAddress
            )
        }
    }
}