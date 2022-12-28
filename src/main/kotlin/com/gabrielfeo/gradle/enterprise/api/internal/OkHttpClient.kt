package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.accessToken
import com.gabrielfeo.gradle.enterprise.api.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.cacheablePaths
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheHitLoggingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.cache
import com.gabrielfeo.gradle.enterprise.api.maxConcurrentRequests
import okhttp3.OkHttpClient

val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(HttpBearerAuth("bearer", accessToken()))
        .addInterceptor(CacheHitLoggingInterceptor())
        .addNetworkInterceptor(CacheEnforcingInterceptor(cacheablePaths))
        .build()
        .apply {
            dispatcher.maxRequests = maxConcurrentRequests
            dispatcher.maxRequestsPerHost = maxConcurrentRequests
        }
}
