package com.spring.localparking.store.repository
import com.spring.localparking.store.domain.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long>{
    fun findByStoreId(storeId: Long): List<Product>
    fun countByStoreId(storeId: Long): Long
}