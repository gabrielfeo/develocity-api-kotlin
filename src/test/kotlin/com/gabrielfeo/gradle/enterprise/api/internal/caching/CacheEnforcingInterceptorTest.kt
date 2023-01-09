package com.gabrielfeo.gradle.enterprise.api.internal.caching

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val SHORT_TERM_CACHE_MAX_AGE = 1L
private const val LONG_TERM_CACHE_MAX_AGE = 2L

class CacheEnforcingInterceptorTest {

    private val interceptor = CacheEnforcingInterceptor(
        shortTermCacheUrlPattern = Regex(".*short.*"),
        shortTermCacheMaxAge = SHORT_TERM_CACHE_MAX_AGE,
        longTermCacheUrlPattern = Regex(".*long.*"),
        longTermCacheMaxAge = LONG_TERM_CACHE_MAX_AGE,
    )

    private val server = MockWebServer()

    private val client = OkHttpClient.Builder()
        .addNetworkInterceptor(interceptor)
        .build()

    private val originalResponse = MockResponse()
        .setHeader("cache-control", "no-cache, no-store, must-revalidate")
        .setHeader("expires", "0")
        .setHeader("pragma", "no-cache")
        .setHeader("vary", "origin")

    @BeforeTest
    fun enqueueResponse() {
        server.enqueue(originalResponse)
    }

    @Test
    fun `URL matched for short-term cache`() {
        val response = client.newCall(buildRequest("/short")).execute()
        assertEquals("max-age=$SHORT_TERM_CACHE_MAX_AGE", response.headers["cache-control"])
        assertNull(response.headers["pragma"])
        assertNull(response.headers["expiry"])
        assertNull(response.headers["vary"])
    }

    @Test
    fun `URL matched for long-term cache`() {
        val response = client.newCall(buildRequest("/long")).execute()
        assertEquals("max-age=$LONG_TERM_CACHE_MAX_AGE", response.headers["cache-control"])
        assertNull(response.headers["pragma"])
        assertNull(response.headers["expiry"])
        assertNull(response.headers["vary"])
    }

    @Test
    fun `URL not matched for caching`() {
        val response = client.newCall(buildRequest("/other")).execute()
        assertEquals(originalResponse.headers, response.headers)
    }

    private fun buildRequest(path: String) = Request.Builder()
        .get()
        .url(server.url(path))
        .build()
}
