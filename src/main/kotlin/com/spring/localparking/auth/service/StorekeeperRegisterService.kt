package com.spring.localparking.auth.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.auth.dto.AdminLoginRequest
import com.spring.localparking.auth.dto.TokenResponse
import com.spring.localparking.auth.dto.join.RegisterRequest
import com.spring.localparking.auth.dto.storekeeper.StorekeeperRegisterRequest
import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.category.repository.CategoryRepository
import com.spring.localparking.global.dto.RequestStatus
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.global.util.JwtUtil
import com.spring.localparking.store.domain.Store
import com.spring.localparking.store.domain.StoreCategory
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.user.domain.User
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class StorekeeperRegisterService (
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    private val passwordEncoder: PasswordEncoder,
    private val categoryRepository: CategoryRepository,
    private val termService: TermService,
    private val authenticationManager: AuthenticationManager,
    private val tokenService: TokenService,
    private val jwtUtil: JwtUtil
){
    @Transactional
    fun registerStorekeeper(request: StorekeeperRegisterRequest) {
        if (userRepository.findByAdminId(request.adminId) != null) {
            throw CustomException(ErrorCode.ALREADY_USED_ID)
        }
        if (userRepository.findByEmail(request.email) != null) {
            throw CustomException(ErrorCode.ALREADY_USED_EMAIL)
        }
        storeRepository.findByBusinessNumber(request.businessNumber)?.let {
            if (it.owner != null) {
                throw CustomException(ErrorCode.STOREKEEPER_ALREADY_EXIST)
            }
        }
        val encodedPassword = passwordEncoder.encode(request.password)
        val user = User.ofStorekeeper(request, encodedPassword)
        val savedUser = userRepository.saveAndFlush(user)
        if (request.storeId != null) {
            val existingStore = storeRepository.findById(request.storeId)
                .orElseThrow { CustomException(ErrorCode.STORE_NOT_FOUND) }

            if (existingStore.owner != null) {
                throw CustomException(ErrorCode.STOREKEEPER_ALREADY_EXIST)
            }
            existingStore.owner = savedUser
            existingStore.businessNumber = request.businessNumber
            storeRepository.save(existingStore)

        } else {
            if (request.storeName.isNullOrBlank() || request.storeAddress == null || request.categoryId == null) {
                throw CustomException(ErrorCode.STORE_NAME_REQUIRED)
            } else {
                val categoryId = request.categoryId
                val newStore = Store.of(request, savedUser)
                storeRepository.saveAndFlush(newStore)
                val category = categoryRepository.findById(categoryId)
                    .orElseThrow { CustomException(ErrorCode.CATEGORY_NOT_FOUND) }
                val storeCategory = StoreCategory(store = newStore, category = category)
                newStore.categories.add(storeCategory)
                storeRepository.save(newStore)
            }
            termService.processAgreements(savedUser, RegisterRequest(request.agreements))
        }
    }
    @Transactional
    fun loginStorekeeper(req: AdminLoginRequest): TokenResponse {
        val auth = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(req.adminId, req.password)
        )
        val principal = auth.principal as CustomPrincipal
        val user = userRepository.findById(principal.id!!)
            .orElseThrow { UserNotFoundException() }

        when (user.registrationStatus) {
            RequestStatus.PENDING -> throw CustomException(ErrorCode.PROCESSING_REQUEST)
            RequestStatus.REJECTED -> throw CustomException(ErrorCode.REJECTED_REQUEST)
            RequestStatus.APPROVED -> {
                val accessToken = jwtUtil.generateAccessToken(user.id!!, user.role.value)
                val refreshToken = jwtUtil.generateRefreshToken(user.id!!)
                tokenService.saveRefreshToken(user.id!!, refreshToken)
                return TokenResponse(accessToken, refreshToken)
            }
        }
    }
}