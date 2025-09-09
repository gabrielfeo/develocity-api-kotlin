package com.gabrielfeo.develocity.api.internal

import com.gabrielfeo.develocity.api.*
import com.gabrielfeo.develocity.api.internal.auth.*
import com.gabrielfeo.develocity.api.internal.caching.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

private const val HTTP_LOGGER_NAME = "com.gabrielfeo.develocity.api.OkHttpClient"
private const val CACHE_LOGGER_NAME = "com.gabrielfeo.develocity.api.Cache"

/**
 * Builds the final `OkHttpClient` with a `Config`.
 */
internal fun buildOkHttpClient(
    config: Config,
) = with(config.clientBuilder) {
    readTimeout(Duration.ofMillis(config.readTimeoutMillis))
    val httpLogger = LoggerFactory.getLogger(HTTP_LOGGER_NAME)
    val cacheLogger = LoggerFactory.getLogger(CACHE_LOGGER_NAME)
    if (config.cacheConfig.cacheEnabled) {
        cache(buildCache(config, cacheLogger))
    } else {
        cacheLogger.debug("HTTP cache is disabled")
    }
    addInterceptors(config, cacheLogger)
    addNetworkInterceptors(config, httpLogger)
    build().apply {
        config.maxConcurrentRequests?.let {
            dispatcher.maxRequests = it
            dispatcher.maxRequestsPerHost = it
        }
    }
}

private fun OkHttpClient.Builder.addInterceptors(
    config: Config,
    cacheLogger: Logger,
) {
    if (config.cacheConfig.cacheEnabled) {
        addInterceptor(CacheHitLoggingInterceptor(cacheLogger))
    }
}

private fun OkHttpClient.Builder.addNetworkInterceptors(
    config: Config,
    httpLogger: Logger,
) {
    if (config.cacheConfig.cacheEnabled) {
        addNetworkInterceptor(buildCacheEnforcingInterceptor(config))
    }
    addNetworkInterceptor(HttpLoggingInterceptor(logger = httpLogger::debug).apply { level = BASIC })
    addNetworkInterceptor(HttpLoggingInterceptor(logger = httpLogger::trace).apply { level = BODY })
    // Add authentication after logging to prevent clients from leaking their access key
    addNetworkInterceptor(HttpBearerAuth("bearer", config.accessKey()))
}

internal fun buildCache(
    config: Config,
    cacheLogger: Logger,
): Cache {
    val cacheDir = config.cacheConfig.cacheDir
    val maxSize = config.cacheConfig.maxCacheSize
    cacheLogger.debug("HTTP cache dir: {} (max {}B)", cacheDir, maxSize)
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
