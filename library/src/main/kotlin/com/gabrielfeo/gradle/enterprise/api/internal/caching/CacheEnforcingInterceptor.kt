package com.gabrielfeo.develocity.api.internal.caching

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


internal class CacheEnforcingInterceptor(
    private val longTermCacheUrlPattern: Regex,
    private val longTermCacheMaxAge: Long,
    private val shortTermCacheUrlPattern: Regex,
    private val shortTermCacheMaxAge: Long,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val maxAge = maxAgeFor(response.request)
        if (maxAge <= 0) {
            return response
        }
        return response.newBuilder()
            .header("cache-control", "max-age=$maxAge")
            .removeHeader("pragma")
            .removeHeader("expires")
            .removeHeader("vary")
            .build()
    }

    private fun maxAgeFor(request: Request): Long {
        val url = request.url.toString()
        return when {
            longTermCacheUrlPattern.matches(url) -> longTermCacheMaxAge
            shortTermCacheUrlPattern.matches(url) -> shortTermCacheMaxAge
            else -> 0
        }
    }
}
