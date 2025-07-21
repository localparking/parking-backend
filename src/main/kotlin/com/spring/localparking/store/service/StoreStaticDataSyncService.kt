package com.spring.localparking.store.service

import com.spring.localparking.store.StoreDocumentMapper
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.store.repository.StoreSearchRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class StoreStaticDataSyncService(
    private val storeRepository: StoreRepository,        // JPA
    private val storeSearchRepository: StoreSearchRepository  // ES
) {
    @PostConstruct
    fun init() = syncStores()          // 앱 시작 시 1회

    fun syncStores() {
        val entities = storeRepository.findAll()       // ① RDB 모든 가게
        val docs = entities
            .mapNotNull { StoreDocumentMapper.toDocument(it) }  // ② 문서 매핑
        if (docs.isNotEmpty()) {
            storeSearchRepository.saveAll(docs)           // ③ ES 색인
        }
    }
}
