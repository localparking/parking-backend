package com.spring.localparking.storekeeper.service

import com.spring.global.exception.ErrorCode
import com.spring.localparking.category.repository.CategoryRepository
import com.spring.localparking.global.exception.CustomException
import com.spring.localparking.operatingHour.domain.OperatingHour
import com.spring.localparking.operatingHour.domain.TimeSlot
import com.spring.localparking.store.domain.StoreCategory
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.storekeeper.dto.MyStoreResponse
import com.spring.localparking.storekeeper.dto.MyStoreUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class StorekeeperService(
    private val storeRepository: StoreRepository,
    private val categoryRepository: CategoryRepository
) {
    @Transactional(readOnly = true)
    fun getMyStoreInfo(userId: Long): MyStoreResponse {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        return MyStoreResponse.from(store)
    }

    @Transactional
    fun updateMyStoreInfo(userId: Long, request: MyStoreUpdateRequest) {
        val store = storeRepository.findByOwnerId(userId)
            ?: throw CustomException(ErrorCode.STORE_NOT_FOUND)
        store.categories.clear()
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { CustomException(ErrorCode.CATEGORY_NOT_FOUND) }
        store.categories.add(StoreCategory(store = store, category = category))
        store.tel = request.tel
        store.location.doroAddress?.let { doroAddress ->
            doroAddress.sido = request.address.sido
            doroAddress.sigungu = request.address.sigungu
            doroAddress.doroName = request.address.doroName
            doroAddress.buildingNo = request.address.buildingNo
            doroAddress.fullAddress =
                "${request.address.sido} ${request.address.sigungu} ${request.address.doroName} ${request.address.buildingNo}".trim()
        }
        store.location.lat = request.address.lat
        store.location.lon = request.address.lon

        val operatingHour = store.operatingHour ?: OperatingHour()
        operatingHour.timeSlots.clear()

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        request.operatingHours.forEach { dailyDto ->
            val dayOfWeek = KOR_DAY_MAP.entries.find { it.value == dailyDto.label }?.key
            if (dayOfWeek != null) {
                dailyDto.slots.forEach { slotDto ->
                    if (slotDto.begin.isNotBlank() && slotDto.end.isNotBlank()) {
                        val beginTime = LocalTime.parse(slotDto.begin, timeFormatter)
                        val endTime = if (slotDto.end == "24:00") LocalTime.of(23, 59, 59) else LocalTime.parse(
                            slotDto.end,
                            timeFormatter
                        )

                        operatingHour.addTimeSlot(
                            TimeSlot(dayOfWeek = dayOfWeek, beginTime = beginTime, endTime = endTime)
                        )
                    }
                }
            }
        }
        store.operatingHour = operatingHour

        storeRepository.save(store)
    }

    companion object {
        private val KOR_DAY_MAP = mapOf(
            DayOfWeek.MONDAY to "월", DayOfWeek.TUESDAY to "화", DayOfWeek.WEDNESDAY to "수",
            DayOfWeek.THURSDAY to "목", DayOfWeek.FRIDAY to "금", DayOfWeek.SATURDAY to "토",
            DayOfWeek.SUNDAY to "일"
        )
    }
}