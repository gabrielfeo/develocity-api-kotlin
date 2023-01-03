package com.gabrielfeo.gradle.enterprise.api.internal.caching

import com.gabrielfeo.gradle.enterprise.api.Options
import okhttp3.Interceptor
import okhttp3.Response
import java.util.logging.Level
import java.util.logging.Logger

internal class CacheHitLoggingInterceptor(
    private val logger: Logger = Logger.getGlobal(),
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!Options.debugLoggingEnabled) {
            return chain.proceed(chain.request())
        }
        val url = chain.request().url
        val response = chain.proceed(chain.request())
        val wasHit = with(response) { cacheResponse != null && networkResponse == null }
        val hitOrMiss = if (wasHit) "hit" else "miss"
        logger.log(Level.INFO, "Cache $hitOrMiss: $url")
        return response
    }
}
