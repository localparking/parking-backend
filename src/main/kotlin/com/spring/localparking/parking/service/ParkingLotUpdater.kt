package com.spring.localparking.parking.service

import com.spring.localparking.auth.dto.storekeeper.ParkingLotManualRequestDto
import com.spring.localparking.global.util.FeeCalculationUtil
import com.spring.localparking.global.util.OperatingHourParser
import com.spring.localparking.parking.domain.FeePolicy
import com.spring.localparking.parking.domain.ParkingLot
import org.springframework.stereotype.Component

@Component
class ParkingLotUpdater {

    fun updateFromDto(parkingLot: ParkingLot, request: ParkingLotManualRequestDto): ParkingLot {
        parkingLot.name = request.name!!
        parkingLot.address = request.address!!
        parkingLot.tel = request.tel
        parkingLot.capacity = request.capacity

        val feePolicy = parkingLot.feePolicy ?: FeePolicy(
            baseFee = request.baseFee!!,
            baseTimeMin = request.baseTimeMin!!,
            additionalFee = request.additionalFee,
            additionalTimeMin = request.additionalTimeMin,
            dayPassFee = request.dayPassFee
        )
        feePolicy.baseFee = request.baseFee!!
        feePolicy.baseTimeMin = request.baseTimeMin!!
        feePolicy.additionalFee = request.additionalFee
        feePolicy.additionalTimeMin = request.additionalTimeMin
        feePolicy.dayPassFee = request.dayPassFee
        parkingLot.feePolicy = feePolicy
        parkingLot.operatingHour = OperatingHourParser.parse(request.operatingHours)

        parkingLot.hourlyFee = FeeCalculationUtil.calculateHourlyFee(feePolicy)

        return parkingLot
    }
}