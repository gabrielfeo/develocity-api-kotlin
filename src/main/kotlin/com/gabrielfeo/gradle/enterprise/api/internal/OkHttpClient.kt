package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.internal.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheHitLoggingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.buildCache
import okhttp3.OkHttpClient

internal val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .cache(buildCache())
        .addInterceptor(HttpBearerAuth("bearer", Options.accessToken()))
        .addInterceptor(CacheHitLoggingInterceptor())
        .addNetworkInterceptor(buildCacheEnforcingInterceptor())
        .build()
        .apply {
            dispatcher.maxRequests = Options.maxConcurrentRequests
            dispatcher.maxRequestsPerHost = Options.maxConcurrentRequests
        }
}

private fun buildCacheEnforcingInterceptor() = CacheEnforcingInterceptor(
    longTermCacheUrlPattern = Options.longTermCacheUrlPattern,
    longTermCacheMaxAge = Options.longTermCacheMaxAge,
    shortTermCacheUrlPattern = Options.shortTermCacheUrlPattern,
    shortTermCacheMaxAge = Options.shortTermCacheMaxAge,
)
