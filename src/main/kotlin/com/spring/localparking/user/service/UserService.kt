package com.spring.localparking.user.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.auth.service.TermService
import com.spring.localparking.auth.service.TokenService
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.user.dto.MyInfoResponseDto
import com.spring.localparking.user.dto.MyInfoUpdateRequestDto
import com.spring.localparking.user.dto.VisitorInfo
import com.spring.localparking.user.exception.UserNotFoundException
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val termService: TermService
){
    @Transactional
    fun withdrawUser(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }

        user.withdraw()

        tokenService.deleteRefreshToken(userId)
    }
    @Transactional(Transactional.TxType.SUPPORTS)
    fun getMyVisitorInfo(userId: Long): VisitorInfo {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        val userProfile = user.userProfile
            ?: throw CustomException(ErrorCode.USER_PROFILE_NOT_FOUND)
        return VisitorInfo.from(userProfile)
    }
    @Transactional
    fun updateVisitorInfo(userId: Long, visitorInfo: VisitorInfo):VisitorInfo {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        val userProfile = user.userProfile
            ?: throw CustomException(ErrorCode.USER_PROFILE_NOT_FOUND)
        userProfile.updateVisitorInfo(
            name = visitorInfo.name,
            tel = visitorInfo.tel,
            regionName = visitorInfo.regionName,
            vehicleNumber = visitorInfo.vehicleNumber
        )
        return VisitorInfo.from(userProfile)
    }
    @Transactional(Transactional.TxType.SUPPORTS)
    fun getMyInfo(userId: Long): MyInfoResponseDto {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        return MyInfoResponseDto.from(user)
    }
    @Transactional
    fun updateMyInfo(userId: Long, request: MyInfoUpdateRequestDto): MyInfoResponseDto {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        val userProfile = user.userProfile
            ?: throw CustomException(ErrorCode.USER_PROFILE_NOT_FOUND)
        user.updateMyInfo(
            nickname = request.nickname,
            isNotification = request.isNotification
        )
        termService.updateMarketingAgreement(user, request.isNotification)
        userProfile.updateVisitorInfo(
            name = request.name,
            tel = request.tel,
            regionName = request.regionName,
            vehicleNumber = request.vehicleNumber
        )
        return MyInfoResponseDto.from(user)
    }
}
