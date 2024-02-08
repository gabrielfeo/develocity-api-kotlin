package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.Config
import com.gabrielfeo.gradle.enterprise.api.internal.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheHitLoggingInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import java.time.Duration
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Base instance just so that multiple created [Config]s will share resources by default.
 */
internal val basicOkHttpClient by lazy {
    OkHttpClient.Builder().build()
}

/**
 * Builds the final `OkHttpClient` with a `Config`.
 */
internal fun buildOkHttpClient(
    config: Config,
) = with(config.clientBuilder) {
    readTimeout(Duration.ofMillis(config.readTimeoutMillis))
    if (config.cacheConfig.cacheEnabled) {
        cache(buildCache(config))
    }
    addInterceptors(config)
    addNetworkInterceptors(config)
    build().apply {
        config.maxConcurrentRequests?.let {
            dispatcher.maxRequests = it
            dispatcher.maxRequestsPerHost = it
        }
    }
}

private fun OkHttpClient.Builder.addInterceptors(config: Config) {
    if (config.debugLoggingEnabled && config.cacheConfig.cacheEnabled) {
        addInterceptor(CacheHitLoggingInterceptor())
    }
}

private fun OkHttpClient.Builder.addNetworkInterceptors(config: Config) {
    if (config.cacheConfig.cacheEnabled) {
        addNetworkInterceptor(buildCacheEnforcingInterceptor(config))
    }
    if (config.debugLoggingEnabled) {
        addNetworkInterceptor(HttpLoggingInterceptor().apply { level = BODY })
    }
    addNetworkInterceptor(HttpBearerAuth("bearer", config.apiToken()))
}

internal fun buildCache(
    config: Config
): Cache {
    val cacheDir = config.cacheConfig.cacheDir
    val maxSize = config.cacheConfig.maxCacheSize
    if (config.debugLoggingEnabled) {
        val logger = Logger.getGlobal()
        logger.log(Level.INFO, "HTTP cache dir: $cacheDir (max ${maxSize}B)")
    }
    return Cache(cacheDir, maxSize)
}

private fun buildCacheEnforcingInterceptor(
    config: Config,
) = CacheEnforcingInterceptor(
    longTermCacheUrlPattern = config.cacheConfig.longTermCacheUrlPattern,
    longTermCacheMaxAge = config.cacheConfig.longTermCacheMaxAge,
    shortTermCacheUrlPattern = config.cacheConfig.shortTermCacheUrlPattern,
    shortTermCacheMaxAge = config.cacheConfig.shortTermCacheMaxAge,
)
