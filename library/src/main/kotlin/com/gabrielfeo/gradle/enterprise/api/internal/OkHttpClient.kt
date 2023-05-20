package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.Options
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

internal fun buildOkHttpClient(
    options: Options,
) = with(options.clientBuilder) {
    readTimeout(Duration.ofMillis(options.readTimeoutMillis))
    if (options.cacheOptions.cacheEnabled) {
        cache(buildCache(options))
    }
    addInterceptors(options)
    addNetworkInterceptors(options)
    build().apply {
        options.maxConcurrentRequests?.let {
            dispatcher.maxRequests = it
            dispatcher.maxRequestsPerHost = it
        }
    }
}

private fun OkHttpClient.Builder.addInterceptors(options: Options) {
    if (options.debugLoggingEnabled && options.cacheOptions.cacheEnabled) {
        addInterceptor(CacheHitLoggingInterceptor())
    }
}

private fun OkHttpClient.Builder.addNetworkInterceptors(options: Options) {
    if (options.cacheOptions.cacheEnabled) {
        addNetworkInterceptor(buildCacheEnforcingInterceptor(options))
    }
    if (options.debugLoggingEnabled) {
        addNetworkInterceptor(HttpLoggingInterceptor().apply { level = BODY })
    }
    addNetworkInterceptor(HttpBearerAuth("bearer", options.apiToken()))
}

internal fun buildCache(
    options: Options
): Cache {
    val cacheDir = options.cacheOptions.cacheDir
    val maxSize = options.cacheOptions.maxCacheSize
    if (options.debugLoggingEnabled) {
        val logger = Logger.getGlobal()
        logger.log(Level.INFO, "HTTP cache dir: $cacheDir (max ${maxSize}B)")
    }
    return Cache(cacheDir, maxSize)
}

private fun buildCacheEnforcingInterceptor(
    options: Options,
) = CacheEnforcingInterceptor(
    longTermCacheUrlPattern = options.cacheOptions.longTermCacheUrlPattern,
    longTermCacheMaxAge = options.cacheOptions.longTermCacheMaxAge,
    shortTermCacheUrlPattern = options.cacheOptions.shortTermCacheUrlPattern,
    shortTermCacheMaxAge = options.cacheOptions.shortTermCacheMaxAge,
)
