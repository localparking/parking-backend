package com.spring.localparking.auth.dto.join

import java.time.LocalDateTime

data class TermDto (
    val termId: Long,
    val version: String?,
    val title: String?,
    val content: String?,
    val mandatory: Boolean,
    val effectiveDate: LocalDateTime
)