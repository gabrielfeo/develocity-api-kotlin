package com.gabrielfeo.gradle.enterprise.api

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import kotlin.time.Duration.Companion.days

internal val cache: Cache = run {
    val host = baseUrl().toHttpUrl().host
    val tempDir = System.getProperty("java.io.tmpdir")
    Cache(
        directory = File(tempDir, "gradle-enterprise-api-cache-$host"),
        maxSize = maxCacheSize,
    )
}

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