package com.spring.localparking.s3.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.time.Duration
import java.util.*

@Service
class S3PresignedUrlService(
    private val s3Presigner: S3Presigner
) {
    @Value("\${spring.cloud.aws.s3.bucket-name}")
    private lateinit var bucketName: String

    private val PRODUCT_IMAGE_DIR = "products"

    fun getPresignedUrl(originalFilename: String): Map<String, String> {
        val objectKey = "$PRODUCT_IMAGE_DIR/${UUID.randomUUID()}_$originalFilename"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build()

        val presignedRequest = s3Presigner.presignPutObject {
            it.signatureDuration(Duration.ofMinutes(10))
            it.putObjectRequest(putObjectRequest)
        }

        return mapOf(
            "presignedUrl" to presignedRequest.url().toString(),
            "imageKey" to objectKey
        )
    }
}