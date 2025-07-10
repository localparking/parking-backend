package com.spring.localparking.user.domain

import jakarta.persistence.*

@Entity
@Table
class Category (
    @Column(nullable = false, unique = true)
    var code : String,
    @Column(nullable = false)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null,
    @OneToMany(mappedBy = "category")
    var users: MutableList<UserCategory> = mutableListOf()
){
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

}