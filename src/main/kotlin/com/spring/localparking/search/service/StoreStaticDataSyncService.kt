package com.spring.localparking.search.service

import com.spring.localparking.search.dto.StoreDocumentMapper
import com.spring.localparking.store.repository.StoreRepository
import com.spring.localparking.search.repository.store.StoreSearchRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class StoreStaticDataSyncService(
    private val storeRepository: StoreRepository,
    private val storeSearchRepository: StoreSearchRepository
) {
    @PostConstruct
    fun init() = syncStores()

    fun syncStores() {
        val entities = storeRepository.findAll()
        val docs = entities
            .mapNotNull { StoreDocumentMapper.toDocument(it) }
        if (docs.isNotEmpty()) {
            storeSearchRepository.saveAll(docs)
        }
    }
}
