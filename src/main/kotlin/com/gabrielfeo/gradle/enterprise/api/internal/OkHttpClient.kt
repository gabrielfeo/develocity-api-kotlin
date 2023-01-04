package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.internal.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheHitLoggingInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.util.logging.Level
import java.util.logging.Logger

internal val okHttpClient: OkHttpClient by lazy {
    val cache = buildCache()
    with(OkHttpClient.Builder()) {
        cache(cache)
        if (Options.Debugging.debugLoggingEnabled && Options.Cache.cacheEnabled) {
            addInterceptor(CacheHitLoggingInterceptor())
        }
        addInterceptor(HttpBearerAuth("bearer", Options.GradleEnterpriseInstance.accessToken()))
        if (Options.Cache.cacheEnabled) {
            addNetworkInterceptor(buildCacheEnforcingInterceptor())
        }
        build().apply {
            dispatcher.maxRequests = Options.Concurrency.maxConcurrentRequests
            dispatcher.maxRequestsPerHost = Options.Concurrency.maxConcurrentRequests
        }
    }
}

internal fun buildCache(): Cache {
    val cacheDir = Options.Cache.cacheDir
    val maxSize = Options.Cache.maxCacheSize
    if (Options.Debugging.debugLoggingEnabled) {
        val logger = Logger.getGlobal()
        logger.log(Level.INFO, "HTTP cache dir: $cacheDir (max ${maxSize}B)")
    }
    return Cache(cacheDir, maxSize)
}

private fun buildCacheEnforcingInterceptor() = CacheEnforcingInterceptor(
    longTermCacheUrlPattern = Options.Cache.longTermCacheUrlPattern,
    longTermCacheMaxAge = Options.Cache.longTermCacheMaxAge,
    shortTermCacheUrlPattern = Options.Cache.shortTermCacheUrlPattern,
    shortTermCacheMaxAge = Options.Cache.shortTermCacheMaxAge,
)
