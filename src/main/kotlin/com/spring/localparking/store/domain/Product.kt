package com.spring.localparking.store.domain


import jakarta.persistence.*

@Entity
@Table(name = "product")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var imageUrl: String,

    @Column(columnDefinition = "TEXT")
    var description: String,

    @Column(nullable = false)
    var price: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    var store: Store

){
    fun updateProduct(
        name: String,
        imageUrl: String,
        description: String,
        price: Int
    ) {
        this.name = name
        this.imageUrl = imageUrl
        this.description = description
        this.price = price
    }
}