package com.spring.localparking.user.domain

import jakarta.persistence.*

@Entity
@Table(name = "user_category")
@IdClass(UserCategoryId::class)
class UserCategory (
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category
)