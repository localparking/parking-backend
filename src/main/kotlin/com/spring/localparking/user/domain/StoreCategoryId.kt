package com.spring.localparking.user.domain

import java.io.Serializable

data class StoreCategoryId (
    val store: Long = 0,
    val category: Long = 0
) : Serializable