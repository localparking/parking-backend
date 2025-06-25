package com.spring.localparking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LocalParkingApplication

fun main(args: Array<String>) {
    runApplication<LocalParkingApplication>(*args)
}
