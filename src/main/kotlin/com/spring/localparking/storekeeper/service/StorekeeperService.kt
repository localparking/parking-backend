package com.spring.localparking.storekeeper.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.auth.dto.storekeeper.ParkingLotManualRequestDto
import com.spring.localparking.category.repository.CategoryRepository
import com.spring.localparking.global.dto.StoreType
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.global.util.FeeCalculationUtil
import com.spring.localparking.global.util.OperatingHourParser
import com.spring.localparking.operatingHour.OperatingHourPresenter
import com.spring.localparking.parking.domain.FeePolicy
import com.spring.localparking.parking.domain.ParkingLot
import com.spring.localparking.parking.repository.ParkingLotRepository
import com.spring.localparking.parking.service.ParkingLotUpdater
import com.spring.localparking.store.domain.Product
import com.spring.localparking.store.domain.StoreCategory
import com.spring.localparking.store.domain.StoreParkingLot
import com.spring.localparking.store.repository.ProductRepository
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.storekeeper.domain.StoreParkingBenefit
import com.spring.localparking.storekeeper.dto.*
import com.spring.localparking.storekeeper.repository.StoreParkingBenefitRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class StorekeeperService(
    private val storeRepository: StoreRepository,
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val parkingLotRepository: ParkingLotRepository,
    private val parkingLotUpdater: ParkingLotUpdater,
    private val benefitRepository: StoreParkingBenefitRepository
) {
    @Transactional(readOnly = true)
    fun getMyStoreInfo(userId: Long): MyStoreResponse {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        return MyStoreResponse.from(store)
    }

    @Transactional
    fun updateMyStoreInfo(userId: Long, request: MyStoreUpdateRequest): MyStoreResponse {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        store.categories.clear()
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { CustomException(ErrorCode.CATEGORY_NOT_FOUND) }
        store.categories.add(StoreCategory(store = store, category = category))
        store.tel = request.tel
        store.location.doroAddress?.let { doroAddress ->
            doroAddress.sido = request.address.sido!!
            doroAddress.sigungu = request.address.sigungu!!
            doroAddress.doroName = request.address.doroName!!
            doroAddress.buildingNo = request.address.buildingNo!!
            doroAddress.fullAddress =
                "${request.address.sido} ${request.address.sigungu} ${request.address.doroName} ${request.address.buildingNo}".trim()
        }
        store.location.lat = request.address.lat!!
        store.location.lon = request.address.lon!!
        store.operatingHour = OperatingHourParser.parse(request.operatingHours)
        storeRepository.save(store)
        return MyStoreResponse.from(store)
    }

    @Transactional
    fun addProduct(userId: Long, request: ProductRequestDto): ProductResponseDto {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)

        val newProduct = Product(
            name = request.name,
            imageUrl = request.imageUrl,
            description = request.description,
            price = request.price,
            store = store
        )
        store.storeType = StoreType.PRODUCT_DETAIL
        storeRepository.save(store)
        val savedProduct = productRepository.save(newProduct)
        return ProductResponseDto.from(savedProduct)
    }

    @Transactional(readOnly = true)
    fun getProductsByStore(userId: Long): List<ProductResponseDto> {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        return store.products.map { ProductResponseDto.from(it) }
    }

    @Transactional
    fun updateProduct(userId: Long, productId: Long, request: ProductRequestDto): ProductResponseDto {
        val product = productRepository.findById(productId)
            .orElseThrow { CustomException(ErrorCode.PRODUCT_NOT_FOUND) }
        if (product.store.owner?.id != userId) {
            throw CustomException(ErrorCode.ACCESS_DENIED)
        }
        product.updateProduct(
            name = request.name,
            imageUrl = request.imageUrl,
            description = request.description,
            price = request.price
        )
        val updatedProduct = productRepository.save(product)
        return ProductResponseDto.from(updatedProduct)
    }

    @Transactional
    fun deleteProduct(userId: Long, productId: Long) {
        val product = productRepository.findById(productId)
            .orElseThrow { CustomException(ErrorCode.PRODUCT_NOT_FOUND) }
        if (product.store.owner?.id != userId) {
            throw CustomException(ErrorCode.ACCESS_DENIED)
        }
        val store = product.store
        productRepository.delete(product)

        if (productRepository.countByStoreId(store.id) == 0L) {
            store.storeType = StoreType.GENERAL
            storeRepository.save(store)
        }
    }
    private fun toAssociatedParkingLotResponseDto(parkingLot: ParkingLot): AssociatedParkingLotResponseDto {
        val opHours = if (parkingLot.operatingHour != null) OperatingHourPresenter.build(parkingLot.operatingHour) else null
        return AssociatedParkingLotResponseDto(
            parkingCode = parkingLot.parkingCode,
            name = parkingLot.name,
            address = parkingLot.address,
            tel = parkingLot.tel,
            capacity = parkingLot.capacity,
            operatingHours = opHours,
            baseFee = parkingLot.feePolicy!!.baseFee,
            baseTimeMin = parkingLot.feePolicy!!.baseTimeMin,
            additionalFee = parkingLot.feePolicy?.additionalFee,
            additionalTimeMin = parkingLot.feePolicy?.additionalTimeMin,
            dayPassFee = parkingLot.feePolicy?.dayPassFee,
        )
    }

    @Transactional(readOnly = true)
    fun getAssociatedParkingLots(userId: Long): List<AssociatedParkingLotResponseDto> {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        return store.storeParkingLots.map { toAssociatedParkingLotResponseDto(it.parkingLot) }
    }

    fun linkParkingLot(userId: Long, request: LinkParkingRequestDto) {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)

        val parkingLot: ParkingLot = if (!request.parkingCode.isNullOrBlank()) {
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
            val hourlyFee = FeeCalculationUtil.calculateHourlyFee(feePolicy)
            ParkingLot(
                parkingCode = "manual-${UUID.randomUUID()}", // 수동 등록임을 나타내는 접두사
                name = info.name!!,
                address = info.address!!,
                tel = info.tel,
                capacity = info.capacity,
                feePolicy = feePolicy,
                operatingHour = OperatingHourParser.parse(info.operatingHours),
                hourlyFee = hourlyFee,
                lat = info.lat!!,
                lon = info.lon!!,
            ).also { parkingLotRepository.save(it) }
        } else {
            throw CustomException(ErrorCode.REQUIRED_ID_OR_INFO)
        }
        if (store.storeParkingLots.any { it.parkingLot.parkingCode == parkingLot.parkingCode }) {
            throw CustomException(ErrorCode.ALREADY_USED_PARKING_CODE)
        }
        store.storeParkingLots.add(StoreParkingLot(store = store, parkingLot = parkingLot))
        storeRepository.save(store)
    }
    fun updateAssociatedParkingLot(userId: Long, parkingCode: String, request: ParkingLotManualRequestDto) : AssociatedParkingLotResponseDto{
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        val parkingLot = parkingLotRepository.findById(parkingCode)
            .orElseThrow { CustomException(ErrorCode.PARKING_LOT_NOT_FOUND) }

        if (store.storeParkingLots.none { it.parkingLot.parkingCode == parkingCode }) {
            throw CustomException(ErrorCode.ACCESS_DENIED)
        }
        val updatedParkingLot = parkingLotUpdater.updateFromDto(parkingLot, request)
        parkingLotRepository.save(updatedParkingLot)
        return toAssociatedParkingLotResponseDto(updatedParkingLot)
    }

    @Transactional
    fun unlinkParkingLot(userId: Long, parkingCode: String) {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        if (store.storeParkingLots.size <= 1) {
            throw CustomException(ErrorCode.PARKING_LOT_REQUIRED)
        }
        val linkToRemove = store.storeParkingLots.find { it.parkingLot.parkingCode == parkingCode }
            ?: throw CustomException(ErrorCode.PARKING_LOT_NOT_FOUND)
        val parkingLotToRemove = linkToRemove.parkingLot
        storeRepository.save(store)
        store.storeParkingLots.remove(linkToRemove)
        if (parkingLotToRemove.parkingCode.startsWith("manual-")) {
            val isLinkedToOtherStores = parkingLotToRemove.storeParkingLots.any { it.store.id != store.id }
            if (!isLinkedToOtherStores) {
                parkingLotRepository.delete(parkingLotToRemove)
            }
        }
    }

    @Transactional(readOnly = true)
    fun getParkingBenefits(userId: Long): List<ParkingBenefitDto> {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        return store.parkingBenefits.map { ParkingBenefitDto.from(it) }.sortedBy { it.purchaseAmount }
    }

    fun addParkingBenefit(userId: Long, request: ParkingBenefitRequestDto): ParkingBenefitDto {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)

        val newBenefit = StoreParkingBenefit(
            store = store,
            purchaseAmount = request.purchaseAmount!!,
            discountMin = request.discountMin!!
        )
        val savedBenefit = benefitRepository.save(newBenefit)
        return ParkingBenefitDto.from(savedBenefit)
    }

    fun updateParkingBenefit(userId: Long, benefitId: Long, request: ParkingBenefitRequestDto): ParkingBenefitDto {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        val benefit = benefitRepository.findById(benefitId)
            .orElseThrow { CustomException(ErrorCode.BENEFIT_NOT_FOUND) }
        if (benefit.store.id != store.id) {
            throw CustomException(ErrorCode.ACCESS_DENIED)
        }
        benefit.purchaseAmount = request.purchaseAmount!!
        benefit.discountMin = request.discountMin!!
        val updatedBenefit = benefitRepository.save(benefit)
        return ParkingBenefitDto.from(updatedBenefit)
    }

    @Transactional
    fun deleteParkingBenefit(userId: Long, benefitId: Long) {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        val benefit = benefitRepository.findById(benefitId)
            .orElseThrow { CustomException(ErrorCode.BENEFIT_NOT_FOUND) }
        if (benefit.store.id != store.id) {
            throw CustomException(ErrorCode.ACCESS_DENIED)
        }
        benefitRepository.delete(benefit)
    }

}