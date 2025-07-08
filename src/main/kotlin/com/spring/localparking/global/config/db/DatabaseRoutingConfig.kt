package com.spring.localparking.global.config.db

import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.env.Environment
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.orm.jpa.JpaTransactionManager       // ★ 트랜잭션 매니저 추가
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

enum class DbKey { DEV, PROD }

object DbContextHolder {
    private val ctx: ThreadLocal<DbKey> = ThreadLocal.withInitial { DbKey.PROD }
    fun set(key: DbKey)  = ctx.set(key)
    fun get(): DbKey     = ctx.get()
    fun clear()          = ctx.remove()
}

class RoutingDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any = DbContextHolder.get()
}

@Configuration
class DatabaseRoutingConfig {

    @Bean(name = ["devDataSource"])
    fun devDataSource(env: Environment): DataSource =
        DataSourceBuilder.create()
            .url(env.getRequiredProperty("dev.datasource.url"))
            .username(env.getRequiredProperty("dev.datasource.username"))
            .password(env.getRequiredProperty("dev.datasource.password"))
            .type(HikariDataSource::class.java)
            .build()

    @Bean(name = ["prodDataSource"])
    fun prodDataSource(env: Environment): DataSource =
        DataSourceBuilder.create()
            .url(env.getRequiredProperty("prod.datasource.url"))
            .username(env.getRequiredProperty("prod.datasource.username"))
            .password(env.getRequiredProperty("prod.datasource.password"))
            .type(HikariDataSource::class.java)
            .build()

    @Primary
    @Bean
    fun routingDataSource(
        @Qualifier("devDataSource")  dev: DataSource,
        @Qualifier("prodDataSource") prod: DataSource
    ): DataSource =
        RoutingDataSource().apply {
            setTargetDataSources(mapOf(
                DbKey.DEV  to dev,
                DbKey.PROD to prod
            ))
            setDefaultTargetDataSource(prod)
            afterPropertiesSet()
        }

    @Bean
    fun txManager(ds: EntityManagerFactory): PlatformTransactionManager =
        JpaTransactionManager(ds)
}
