package com.spring.localparking.s3.service

import io.awspring.cloud.s3.S3Template
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL

@Service
class ImageUploadService(
    private val s3Template: S3Template
) {
    @Value("\${spring.cloud.aws.s3.bucket-name}")
    private lateinit var bucketName: String

    private val log = LoggerFactory.getLogger(javaClass)

    fun delete(imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) {
            return
        }
        try {
            val key = URL(imageUrl).path.substring(1)
            s3Template.deleteObject(bucketName, key)
            log.info("S3 파일 삭제 성공: {}", imageUrl)
        } catch (e: Exception) {
            log.error("S3 파일 삭제 실패: {}. 원인: {}", imageUrl, e.message)
        }
    }
}