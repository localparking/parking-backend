package com.spring.localparking.search.service

import com.spring.localparking.search.repository.RecentSearchRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SearchCleanupService(
    private val recentSearchRepository: RecentSearchRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    @Scheduled(cron = "0 0 4 * * *")
    fun cleanupOldSearches() {
        val threeDaysAgo = LocalDateTime.now().minusDays(3)
        log.info("[BATCH] 3일이 지난 최근 검색어 삭제 시작. 기준 시각: {}", threeDaysAgo)
        val deletedCount = recentSearchRepository.deleteByCreatedAtBefore(threeDaysAgo)
        log.info("[BATCH] 총 {}개의 오래된 최근 검색어 삭제 완료.", deletedCount)
    }
}