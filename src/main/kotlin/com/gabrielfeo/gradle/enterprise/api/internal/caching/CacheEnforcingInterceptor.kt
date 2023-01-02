package com.gabrielfeo.gradle.enterprise.api.internal.caching

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import kotlin.time.Duration.Companion.days

internal class CacheEnforcingInterceptor(
    private val cacheableUrlPattern: Regex,
) : Interceptor {

    private val maxAge = 365.days.inWholeMilliseconds

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!isCacheable(response.request)) {
            return response
        }
        return response.newBuilder()
            .header("cache-control", "max-age=$maxAge")
            .removeHeader("pragma")
            .removeHeader("expires")
            .removeHeader("vary")
            .build()
    }

    private fun isCacheable(request: Request) =
        cacheableUrlPattern.matches(request.url.toString())
}
