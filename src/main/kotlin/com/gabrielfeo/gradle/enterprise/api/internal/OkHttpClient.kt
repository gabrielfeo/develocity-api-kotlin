package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.internal.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheHitLoggingInterceptor
import okhttp3.Cache
import java.util.logging.Level
import java.util.logging.Logger

internal val okHttpClient by lazy {
    buildOkHttpClient(options = options)
}

internal fun buildOkHttpClient(
    options: Options,
) = with(options.httpClient.clientBuilder()) {
    if (options.cache.cacheEnabled) {
        cache(buildCache(options))
    }
    if (options.debugging.debugLoggingEnabled && options.cache.cacheEnabled) {
        addInterceptor(CacheHitLoggingInterceptor())
    }
    addInterceptor(HttpBearerAuth("bearer", options.gradleEnterpriseInstance.token()))
    if (options.cache.cacheEnabled) {
        addNetworkInterceptor(buildCacheEnforcingInterceptor(options))
    }
    build().apply {
        options.httpClient.maxConcurrentRequests?.let {
            dispatcher.maxRequests = it
            dispatcher.maxRequestsPerHost = it
        }
    }
}

internal fun buildCache(
    options: Options
): Cache {
    val cacheDir = options.cache.cacheDir
    val maxSize = options.cache.maxCacheSize
    if (options.debugging.debugLoggingEnabled) {
        val logger = Logger.getGlobal()
        logger.log(Level.INFO, "HTTP cache dir: $cacheDir (max ${maxSize}B)")
    }
    return Cache(cacheDir, maxSize)
}

private fun buildCacheEnforcingInterceptor(
    options: Options,
) = CacheEnforcingInterceptor(
    longTermCacheUrlPattern = options.cache.longTermCacheUrlPattern,
    longTermCacheMaxAge = options.cache.longTermCacheMaxAge,
    shortTermCacheUrlPattern = options.cache.shortTermCacheUrlPattern,
    shortTermCacheMaxAge = options.cache.shortTermCacheMaxAge,
)
