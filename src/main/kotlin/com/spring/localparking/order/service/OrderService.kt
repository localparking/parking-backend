package com.spring.localparking.order.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.global.dto.OrderStatus
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.order.domain.Order
import com.spring.localparking.order.domain.OrderItem
import com.spring.localparking.order.dto.*
import com.spring.localparking.order.repository.OrderRepository
import com.spring.localparking.parking.domain.FeePolicy
import com.spring.localparking.store.repository.ProductRepository
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.storekeeper.domain.StoreParkingBenefit
import com.spring.localparking.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.ceil

@Service
class OrderService (
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository,
    private val webClient: WebClient,

    @Value("\${payments.toss.secret-key}") private val secretKey: String

){
    @Transactional
    fun placeOrder(userId: Long, storeId: Long, request: OrderRequestDto): PaymentWidgetInfoResponseDto {
        val user = userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        val store = storeRepository.findById(storeId)
            .orElseThrow { CustomException(ErrorCode.STORE_NOT_FOUND) }
        val productIds = request.orderItems.map {it.productId}
        val products = productRepository.findAllById(productIds).associateBy { it.id }
        if (products.size != productIds.distinct().size) {
            throw CustomException(ErrorCode.PRODUCT_NOT_FOUND) }
        var serverTotalPrice = 0
        request.orderItems.forEach { item ->
            val product = products[item.productId]!!
            if(product.store.id != storeId) {
                throw CustomException(ErrorCode.PRODUCT_NOT_IN_STORE) }
            serverTotalPrice += product.price * item.quantity
        }
        if(serverTotalPrice != request.clientTotalPrice) {
            throw CustomException(ErrorCode.PRICE_MISMATCH)
        }
        val parkingDiscountMin = calculateParkingDiscount(store.parkingBenefits.toList(), serverTotalPrice)
        val parkingFeeDiscount = store.storeParkingLots.firstOrNull()?.parkingLot?.feePolicy?.let { feePolicy ->
            calculateParkingFeeForDuration(parkingDiscountMin, feePolicy)
        } ?: 0
        val totalDiscount = 0
        val order = Order(
            store = store,
            user = user,
            visitorName = request.visitorInfo.name!!,
            visitorTel = request.visitorInfo.tel!!,
            visitorRegionName = request.visitorInfo.regionName!!,
            visitorVehicleNumber = request.visitorInfo.vehicleNumber!!,
            visitTime = request.visitTime,
            totalPrice = serverTotalPrice,
            totalDiscount = totalDiscount,
            parkingFeeDiscount = parkingFeeDiscount,
            parkingDiscountMin = parkingDiscountMin,
            status = OrderStatus.PENDING
        )
        request.orderItems.forEach { itemDto ->
            val product = products[itemDto.productId]!!
            val orderItem = OrderItem(
                order = order,
                product = product,
                quantity = itemDto.quantity,
                orderPrice = product.price
            )
            order.orderItems.add(orderItem)
        }
        val savedOrder = orderRepository.save(order)
        return PaymentWidgetInfoResponseDto.from(savedOrder)
    }
    private fun calculateParkingDiscount(benefits: List<StoreParkingBenefit>, totalPrice: Int): Int {
        return benefits
            .filter { it.purchaseAmount <= totalPrice }
            .maxByOrNull { it.purchaseAmount }
            ?.discountMin ?: 0
    }
    private fun calculateParkingFeeForDuration(durationMin: Int, feePolicy: FeePolicy): Int {
        if (durationMin <= 0) return 0

        if (durationMin <= feePolicy.baseTimeMin) {
            return feePolicy.baseFee
        }
        var totalFee = feePolicy.baseFee
        val additionalTime = feePolicy.additionalTimeMin
        val additionalFee = feePolicy.additionalFee
        if (additionalTime != null && additionalFee != null && additionalTime > 0) {
            val remainingMin = durationMin - feePolicy.baseTimeMin
            val chunks = ceil(remainingMin.toDouble() / additionalTime.toDouble()).toInt()
            totalFee += chunks * additionalFee
        }

        return totalFee
    }

    @Transactional
    fun confirmPayment(paymentKey: String, orderId: String, amount: Int){
        val order = orderRepository.findById(orderId)
            .orElseThrow { CustomException(ErrorCode.ORDER_NOT_FOUND) }
        if (order.totalPrice != amount) {
            throw CustomException(ErrorCode.PRICE_MISMATCH)
        }
        val encodedSecretKey = Base64.getEncoder().encodeToString((secretKey + ":").toByteArray(StandardCharsets.UTF_8))
        val response = webClient
            .mutate()
            .baseUrl("https://api.tosspayments.com")
            .build()
            .post()
            .uri("/v1/payments/confirm")
            .header("Authorization", "Basic $encodedSecretKey")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("paymentKey" to paymentKey, "orderId" to orderId, "amount" to amount))
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()
        if (response != null && response["status"] == "DONE") {
            order.status = OrderStatus.PAID
            order.paymentKey = paymentKey
            orderRepository.save(order)
        } else {
            order.status = OrderStatus.FAILED
            orderRepository.save(order)
            throw CustomException(ErrorCode.PAYMENT_FAILED)
        }
    }
    @Transactional(readOnly = true)
    fun getPaidOrderDetail(userId: Long, orderId: String): OrderResponseDto {
        val order = orderRepository.findById(orderId)
            .orElseThrow { CustomException(ErrorCode.ORDER_NOT_FOUND) }
        if (order.user.id != userId) {
            throw CustomException(ErrorCode.ACCESS_DENIED)
        }
        if (order.status != OrderStatus.PAID) {
            throw CustomException(ErrorCode.NOT_YET_PAID)
        }
        return OrderResponseDto.from(order)
    }
}