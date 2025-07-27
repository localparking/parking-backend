package com.spring.localparking.admin.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.admin.dto.StoreOwnershipReqResponse
import com.spring.localparking.global.dto.RequestStatus
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.storekeeper.repository.StoreOwnershipRequestRepository
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class AdminService(
    private val storeRepository: StoreRepository,
    private val userRepository: UserRepository,
    private val storeOwnershipRequestRepository: StoreOwnershipRequestRepository
) {
    /**
     * 관리자가 소유권 신청을 처리 (승인 또는 거절)
     */
    @Transactional
    fun processOwnershipRequest(requestId: Long, isApproved: Boolean) {
        val request = storeOwnershipRequestRepository.findById(requestId)
            .orElseThrow { CustomException(ErrorCode.NOT_FOUND_STORE_OWNERSHIP_REQUEST) }
        if (request.status != RequestStatus.PENDING) {
            throw CustomException(ErrorCode.STORE_ALREADY_REQUEST)
        }
        val store = request.store
        val user = request.user
        if (isApproved) {
            request.status = RequestStatus.APPROVED
            store.owner = user
            storeRepository.save(store)
            userRepository.save(user)
        } else {
            request.status = RequestStatus.REJECTED
        }
        request.processedAt = LocalDateTime.now()
        storeOwnershipRequestRepository.save(request)
    }

    /**
     * 관리자가 특정 상태의 신청 목록을 조회
     */
    fun getOwnershipRequests(status: RequestStatus): List<StoreOwnershipReqResponse> {
        return storeOwnershipRequestRepository.findByStatus(status)
            .map { request -> StoreOwnershipReqResponse.from(request) }
    }
}