package com.spring.localparking.s3.dto

data class PresignedUrlResponse (
    val presignedUrl: String,
    val imageKey: String
)