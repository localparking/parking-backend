package com.spring.localparking.search.repository.store

import com.spring.localparking.search.domain.StoreDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface StoreSearchRepository : ElasticsearchRepository<StoreDocument, Long>, StoreSearchRepositoryCustom {
}
