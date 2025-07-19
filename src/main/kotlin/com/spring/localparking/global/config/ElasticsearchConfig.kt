package com.spring.localparking.global.config

import com.spring.localparking.operatingHour.config.DayOfWeekToStringConverter
import com.spring.localparking.operatingHour.config.StringToDayOfWeekConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions

@Configuration
class ElasticsearchConfig {

    @Bean
    fun elasticsearchCustomConversions(): ElasticsearchCustomConversions {
        return ElasticsearchCustomConversions(
            listOf(
                DayOfWeekToStringConverter(),
                StringToDayOfWeekConverter()
            )
        )
    }
}