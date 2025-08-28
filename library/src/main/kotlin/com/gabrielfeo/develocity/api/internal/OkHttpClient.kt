package com.gabrielfeo.develocity.api.internal

import com.gabrielfeo.develocity.api.*
import com.gabrielfeo.develocity.api.internal.auth.*
import com.gabrielfeo.develocity.api.internal.caching.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import java.time.Duration

/**
 * Builds the final `OkHttpClient` with a `Config`.
 */
internal fun buildOkHttpClient(
    config: Config,
    loggerFactory: LoggerFactory,
) = with(config.clientBuilder) {
    readTimeout(Duration.ofMillis(config.readTimeoutMillis))
    if (config.cacheConfig.cacheEnabled) {
        cache(buildCache(config, loggerFactory))
    }
    addInterceptors(config, loggerFactory)
    addNetworkInterceptors(config, loggerFactory)
    build().apply {
        config.maxConcurrentRequests?.let {
            dispatcher.maxRequests = it
            dispatcher.maxRequestsPerHost = it
        }
    }
}

private fun OkHttpClient.Builder.addInterceptors(
    config: Config,
    loggerFactory: LoggerFactory,
) {
    if (config.cacheConfig.cacheEnabled) {
        val logger = loggerFactory.newLogger(CacheHitLoggingInterceptor::class)
        addInterceptor(CacheHitLoggingInterceptor(logger))
    }
}

private fun OkHttpClient.Builder.addNetworkInterceptors(
    config: Config,
    loggerFactory: LoggerFactory,
) {
    if (config.cacheConfig.cacheEnabled) {
        addNetworkInterceptor(buildCacheEnforcingInterceptor(config))
    }
    val logger = loggerFactory.newLogger(HttpLoggingInterceptor::class)
    addNetworkInterceptor(HttpLoggingInterceptor(logger = logger::debug).apply { level = BASIC })
    addNetworkInterceptor(HttpLoggingInterceptor(logger = logger::trace).apply { level = BODY })
    // Add authentication after logging to prevent clients from leaking their access key
    addNetworkInterceptor(HttpBearerAuth("bearer", config.accessKey()))
}

internal fun buildCache(
    config: Config,
    loggerFactory: LoggerFactory,
): Cache {
    val cacheDir = config.cacheConfig.cacheDir
    val maxSize = config.cacheConfig.maxCacheSize
    val logger = loggerFactory.newLogger(Cache::class)
    logger.debug("HTTP cache dir: {} (max {}B)", cacheDir, maxSize)
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
