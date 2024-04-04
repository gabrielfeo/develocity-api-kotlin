package com.gabrielfeo.gradle.enterprise.api.internal.caching

import okhttp3.Interceptor
import okhttp3.Response
import org.slf4j.Logger


internal class CacheHitLoggingInterceptor(
    private val logger: Logger,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url
        val response = chain.proceed(chain.request())
        val wasHit = with(response) { cacheResponse != null && networkResponse == null }
        val hitOrMiss = if (wasHit) "hit" else "miss"
        logger.debug("Cache {}: {}", hitOrMiss, url)
        return response
    }
}
