package com.spring.localparking.search.repository

import com.spring.localparking.search.domain.RecentSearch
import com.spring.localparking.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RecentSearchRepository: JpaRepository<RecentSearch, Long>{
    fun findByUserOrderByCreatedAtDesc(user: User): List<RecentSearch>

    fun findByUserAndQuery(user: User, query: String): RecentSearch?

    fun deleteByUserAndQuery(user: User, query: String)

    fun deleteByCreatedAtBefore(cutoffDate: LocalDateTime): Long
}