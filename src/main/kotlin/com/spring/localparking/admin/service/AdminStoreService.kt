package com.spring.localparking.admin.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.admin.dto.StorekeeperRequestResponse
import com.spring.localparking.global.dto.RequestStatus
import com.spring.localparking.global.dto.Role
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AdminStoreService(
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository
) {
    fun getStorekeeperRequests(status: RequestStatus): List<StorekeeperRequestResponse> {
        val users = userRepository.findByRoleAndRegistrationStatus(Role.STOREKEEPER, status)
        return users.map { user ->
            val store = storeRepository.findByOwnerId(user.id!!)
            StorekeeperRequestResponse.from(user, store)
        }
    }
    @Transactional
    fun processStorekeeperRequest(adminId: String, isApproved: Boolean) {
        val user = userRepository.findByAdminId(adminId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        if (user.registrationStatus != RequestStatus.PENDING) {
            throw CustomException(ErrorCode.STORE_ALREADY_REQUEST)
        }
        if (isApproved) {
            user.registrationStatus = RequestStatus.APPROVED
            userRepository.save(user)
        } else {
            user.registrationStatus = RequestStatus.REJECTED
            userRepository.save(user)

            storeRepository.findByOwnerId(user.id!!)?.let { store ->
                store.owner = null
                storeRepository.save(store)
            }
        }
    }
}