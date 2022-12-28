package com.gabrielfeo.gradle.enterprise.api.internal.caching

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import kotlin.time.Duration.Companion.days

internal class CacheEnforcingInterceptor(
    private val cacheablePaths: List<Regex>,
) : Interceptor {

    private val maxAge = 365.days.inWholeMilliseconds

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!isCacheable(request)) {
            return chain.proceed(request)
        }
        val response = chain.proceed(request)
        return response.newBuilder()
            .header("cache-control", "max-age=$maxAge")
            .removeHeader("pragma")
            .removeHeader("expires")
            .removeHeader("vary")
            .build()
    }

    private fun isCacheable(request: Request) =
        cacheablePaths.any { it.matches(request.url.toString()) }
}
