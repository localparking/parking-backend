package com.spring.localparking.storekeeper.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.global.dto.RequestStatus
import com.spring.localparking.global.dto.StoreType
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.store.domain.Product
import com.spring.localparking.store.domain.Store
import com.spring.localparking.store.repository.ProductRepository
import com.spring.localparking.storekeeper.domain.StoreParkingBenefit
import com.spring.localparking.storekeeper.repository.StoreParkingBenefitRepository
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.storekeeper.dto.*
import com.spring.localparking.storekeeper.repository.StoreOwnershipRequestRepository
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class StorekeeperService (
    private val storeRepository: StoreRepository,
    private val storeParkingBenefitRepository: StoreParkingBenefitRepository,
    private val userRepository: UserRepository,
    private val storeOwnershipRequestRepository: StoreOwnershipRequestRepository,
    private val productRepository: ProductRepository
){
    @Transactional
    fun requestOwnership(storeId: Long, userId: Long) {
        val store = storeRepository.findById(storeId)
            .orElseThrow { CustomException(ErrorCode.STORE_NOT_FOUND) }
        val user = userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        if (store.owner != null) {
            throw CustomException(ErrorCode.NOT_FOUND_STORE_OWNERSHIP_REQUEST)
        }
        val existingRequest = storeOwnershipRequestRepository.findByStoreIdAndUserIdAndStatus(storeId, userId, RequestStatus.PENDING)
        if (existingRequest != null) {
            throw CustomException(ErrorCode.NOT_FOUND_STORE_OWNERSHIP_REQUEST)
        }
        val request = StoreOwnershipRequest(store = store, user = user)
        storeOwnershipRequestRepository.save(request)
    }


    @Transactional
    fun addParkingBenefit(storeId: Long, userId: Long, request: BenefitRequestDto) {
        val store = storeRepository.findById(storeId)
            .orElseThrow { CustomException(ErrorCode.STORE_NOT_FOUND) }

        verifyStoreOwner(store, userId)

        val newBenefit = StoreParkingBenefit(
            store = store,
            purchaseAmount = request.purchaseAmount,
            discountMin = request.discountMin
        )
        store.parkingBenefits.add(newBenefit)
        storeRepository.save(store)
    }

    @Transactional
    fun updateParkingBenefit(benefitId: Long, userId: Long, request: BenefitRequestDto) {
        val benefit = storeParkingBenefitRepository.findById(benefitId)
            .orElseThrow { CustomException(ErrorCode.STORE_NOT_FOUND) }
        verifyStoreOwner(benefit.store, userId)

        benefit.purchaseAmount = request.purchaseAmount
        benefit.discountMin = request.discountMin
        storeParkingBenefitRepository.save(benefit)
    }

    @Transactional
    fun deleteParkingBenefit(benefitId: Long, userId: Long) {
        val benefit = storeParkingBenefitRepository.findById(benefitId)
            .orElseThrow { CustomException(ErrorCode.STORE_NOT_FOUND) }

        verifyStoreOwner(benefit.store, userId)

        storeParkingBenefitRepository.deleteById(benefitId)
    }

    private fun verifyStoreOwner(store: Store, userId: Long) {
        if (store.owner?.id != userId) {
            throw CustomException(ErrorCode.ACCESS_DENIED)
        }
    }

    @Transactional
    fun getMyStores(userId: Long): List<MyStoreInfo> {
        val user = userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        val stores = storeRepository.findByOwnerId(user.id!!)

        return stores.map { store ->
            MyStoreInfo(
                storeId = store.id,
                storeName = store.name,
                storeAddress = store.location.doroAddress?.fullAddress
                    ?: store.location.jibeonAddress?.fullAddress ?: "",
                storePhone = store.tel,
                ownerName = store.owner?.nickname,
                parkingBenefits = store.parkingBenefits.map { benefit ->
                    ParkingBenefitDto.from(benefit)
                }
            )
        }
    }
    @Transactional
    fun addProduct(storeId: Long, userId: Long, request: ProductRequestDto) {
        val store = storeRepository.findById(storeId)
            .orElseThrow { CustomException(ErrorCode.STORE_NOT_FOUND) }
        verifyStoreOwner(store, userId)
        val newProduct = Product(
            name = request.name,
            imageUrl = request.imageUrl,
            description = request.description,
            price = request.price,
            store = store
        )
        store.products.add(newProduct)
        store.storeType = StoreType.PRODUCT_DETAIL
        storeRepository.save(store)
    }
    @Transactional
    fun updateProduct(productId: Long, userId: Long, request: ProductRequestDto) {
        val product = productRepository.findById(productId)
            .orElseThrow { CustomException(ErrorCode.PRODUCT_NOT_FOUND) }

        verifyStoreOwner(product.store, userId)

        product.updateProduct(
            name = request.name,
            imageUrl = request.imageUrl,
            description = request.description,
            price = request.price
        )

        productRepository.save(product)
    }

    @Transactional
    fun deleteProduct(productId: Long, userId: Long) {
        val product = productRepository.findById(productId)
            .orElseThrow { CustomException(ErrorCode.PRODUCT_NOT_FOUND) }

        verifyStoreOwner(product.store, userId)

        productRepository.delete(product)
    }
}