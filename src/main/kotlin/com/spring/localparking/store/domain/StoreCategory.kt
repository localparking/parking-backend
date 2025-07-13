package com.spring.localparking.store.domain

import com.spring.localparking.user.domain.Category
import com.spring.localparking.user.domain.StoreCategoryId
import com.spring.localparking.user.domain.User
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "store_category")
@IdClass(StoreCategoryId::class)
class StoreCategory (
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    var store: Store,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category

)