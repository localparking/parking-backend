package com.spring.localparking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
class LocalParkingApplication

fun main(args: Array<String>) {
    runApplication<LocalParkingApplication>(*args)
}
