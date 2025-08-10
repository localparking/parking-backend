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
import com.spring.localparking.global.util.FeeCalculationUtil
import com.spring.localparking.global.util.JwtUtil
import com.spring.localparking.global.util.OperatingHourParser
import com.spring.localparking.parking.domain.FeePolicy
import com.spring.localparking.parking.domain.ParkingLot
import com.spring.localparking.parking.repository.ParkingLotRepository
import com.spring.localparking.store.domain.Store
import com.spring.localparking.store.domain.StoreCategory
import com.spring.localparking.store.domain.StoreParkingLot
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.user.domain.User
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class StorekeeperRegisterService (
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    private val passwordEncoder: PasswordEncoder,
    private val categoryRepository: CategoryRepository,
    private val termService: TermService,
    private val authenticationManager: AuthenticationManager,
    private val tokenService: TokenService,
    private val jwtUtil: JwtUtil,
    private val parkingLotRepository: ParkingLotRepository
){

    fun isAdminIdExists(adminId: String): Boolean {
        return userRepository.findByAdminId(adminId) != null
    }
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
        val store: Store
        if (request.storeId != null) {
            store = storeRepository.findById(request.storeId)
                .orElseThrow { CustomException(ErrorCode.STORE_NOT_FOUND) }

            if (store.owner != null) {
                throw CustomException(ErrorCode.STOREKEEPER_ALREADY_EXIST)
            }
            store.owner = savedUser
            store.businessNumber = request.businessNumber

        } else if (request.storeInfo != null) {
            val storeInfo = request.storeInfo
            store = Store.of(storeInfo, request.businessNumber, savedUser)
            store.operatingHour = OperatingHourParser.parse(storeInfo.operatingHours)

            storeRepository.saveAndFlush(store)

            val category = categoryRepository.findById(storeInfo.categoryId!!)
                .orElseThrow { CustomException(ErrorCode.CATEGORY_NOT_FOUND) }
            store.categories.add(StoreCategory(store = store, category = category))

        } else {
            throw CustomException(ErrorCode.REQUIRED_ID_OR_INFO)
        }
        storeRepository.save(store)
        val parkingLot: ParkingLot? =
            if (!request.parkingCode.isNullOrBlank()) {
            parkingLotRepository.findById(request.parkingCode)
                .orElseThrow { CustomException(ErrorCode.PARKING_LOT_NOT_FOUND) }

        } else if (request.parkingLotInfo != null) {
            val info = request.parkingLotInfo
            val feePolicy = FeePolicy(
                baseFee = info.baseFee!!,
                baseTimeMin = info.baseTimeMin!!,
                additionalFee = info.additionalFee,
                additionalTimeMin = info.additionalTimeMin,
                dayPassFee = info.dayPassFee
            )
            val operatingHour = OperatingHourParser.parse(info.operatingHours)
            val hourlyFee = FeeCalculationUtil.calculateHourlyFee(feePolicy)
            val newParkingLot = ParkingLot(
                parkingCode = "manual-${UUID.randomUUID()}",
                name = info.name!!,
                address = info.address!!,
                tel = info.tel,
                capacity = info.capacity,
                feePolicy = feePolicy,
                operatingHour = operatingHour,
                hourlyFee = hourlyFee,
                lat = info.lat!!,
                lon = info.lon!!,
            )
            parkingLotRepository.save(newParkingLot)
        } else {
                throw CustomException(ErrorCode.REQUIRED_ID_OR_INFO)
        }
        if (parkingLot != null) {
            val storeParkingLot = StoreParkingLot(store = store, parkingLot = parkingLot)
            store.storeParkingLots.add(storeParkingLot)
            storeRepository.save(store)
        }
        termService.processAgreements(savedUser, RegisterRequest(request.agreements))
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