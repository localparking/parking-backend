package com.spring.localparking.store.repository

import com.spring.localparking.store.domain.StoreDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface StoreSearchRepository : ElasticsearchRepository<StoreDocument, Long>, StoreSearchRepositoryCustom {
}
