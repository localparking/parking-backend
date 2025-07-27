package com.spring.localparking.global.dto

enum class Role(val value: String) {
    GUEST("ROLE_GUEST"),
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    STOREKEEPER("ROLE_STOREKEEPER"),
    WITHDRAWN("ROLE_WITHDRAWN");
}