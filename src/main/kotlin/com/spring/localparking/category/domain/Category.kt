package com.spring.localparking.category.domain

import com.spring.localparking.store.domain.StoreCategory
import com.spring.localparking.user.domain.UserCategory
import jakarta.persistence.*

@Entity
@Table
class Category (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    @Column(nullable = false, unique = true)
    var code : String,
    @Column(nullable = false)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null,

    @OneToMany(mappedBy = "category")
    var users: MutableList<UserCategory> = mutableListOf(),

    @OneToMany(mappedBy = "category")
    var stores: MutableList<StoreCategory> = mutableListOf()
)