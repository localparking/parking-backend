package com.spring.localparking.admin.dto

import com.spring.localparking.storekeeper.dto.StoreOwnershipRequest


data class StoreOwnershipReqResponse (
    val requestId: Long,
    val storeId: Long,
    val userId: Long,
    val status: String,
    val storeName: String,
    val userName: String
){
    companion object {
        fun from(request: StoreOwnershipRequest): StoreOwnershipReqResponse {
            return StoreOwnershipReqResponse(
                requestId = request.id,
                storeId = request.store.id,
                userId = request.user.id!!,
                status = request.status.name,
                storeName = request.store.name,
                userName = request.user.nickname
            )
        }
    }
}