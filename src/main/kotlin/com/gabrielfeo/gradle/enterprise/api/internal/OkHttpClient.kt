package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.internal.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheHitLoggingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.cache
import com.gabrielfeo.gradle.enterprise.api.accessToken
import com.gabrielfeo.gradle.enterprise.api.longTermCacheMaxAge
import com.gabrielfeo.gradle.enterprise.api.longTermCacheUrlPattern
import com.gabrielfeo.gradle.enterprise.api.maxConcurrentRequests
import com.gabrielfeo.gradle.enterprise.api.shortTermCacheMaxAge
import com.gabrielfeo.gradle.enterprise.api.shortTermCacheUrlPattern
import okhttp3.OkHttpClient

val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(HttpBearerAuth("bearer", accessToken()))
        .addInterceptor(CacheHitLoggingInterceptor())
        .addNetworkInterceptor(buildCacheEnforcingInterceptor())
        .build()
        .apply {
            dispatcher.maxRequests = maxConcurrentRequests
            dispatcher.maxRequestsPerHost = maxConcurrentRequests
        }
}

private fun buildCacheEnforcingInterceptor() = CacheEnforcingInterceptor(
    longTermCacheUrlPattern = longTermCacheUrlPattern,
    longTermCacheMaxAge = longTermCacheMaxAge,
    shortTermCacheUrlPattern = shortTermCacheUrlPattern,
    shortTermCacheMaxAge = shortTermCacheMaxAge,
)
