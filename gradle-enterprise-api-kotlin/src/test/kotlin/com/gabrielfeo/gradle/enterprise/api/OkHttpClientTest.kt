package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.*
import com.gabrielfeo.gradle.enterprise.api.internal.FakeKeychain
import com.gabrielfeo.gradle.enterprise.api.internal.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheHitLoggingInterceptor
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import kotlin.test.*

class OkHttpClientTest {

    @Test
    fun `Adds authentication`() {
        val client = buildClient()
        assertTrue(client.networkInterceptors.any { it is HttpBearerAuth })
    }

    @Test
    fun `Given maxConcurrentRequests, sets values in Dispatcher`() {
        val client = buildClient(
            "GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS" to "123"
        )
        assertEquals(123, client.dispatcher.maxRequests)
        assertEquals(123, client.dispatcher.maxRequestsPerHost)
    }

    @Test
    fun `Given no maxConcurrentRequests, preserves original client's Dispatcher values`() {
        val baseClient = OkHttpClient.Builder()
            .dispatcher(
                Dispatcher().apply {
                    maxRequests = 1
                    maxRequestsPerHost = 1
                }
            ).build()
        val client = buildClient(clientBuilder = baseClient.newBuilder())
        assertEquals(1, client.dispatcher.maxRequests)
        assertEquals(1, client.dispatcher.maxRequestsPerHost)
    }

    @Test
    fun `Given debug logging and cache enabled, adds logging interceptors`() {
        val client = buildClient(
            "GRADLE_ENTERPRISE_API_DEBUG_LOGGING" to "true",
            "GRADLE_ENTERPRISE_API_CACHE_ENABLED" to "true",
        )
        assertTrue(client.interceptors.any { it is CacheHitLoggingInterceptor })
    }

    @Test
    fun `Given debug logging disabled, doesn't add logging interceptors`() {
        val client = buildClient(
            "GRADLE_ENTERPRISE_API_DEBUG_LOGGING" to "false",
            "GRADLE_ENTERPRISE_API_CACHE_ENABLED" to "true",
        )
        assertTrue(client.interceptors.none { it is CacheHitLoggingInterceptor })
    }

    @Test
    fun `Given cache enabled, configures caching`() {
        val client = buildClient("GRADLE_ENTERPRISE_API_CACHE_ENABLED" to "true")
        assertTrue(client.networkInterceptors.any { it is CacheEnforcingInterceptor })
        assertNotNull(client.cache)
    }

    @Test
    fun `Given cache disabled, no caching or cache logging`() {
        val client = buildClient("GRADLE_ENTERPRISE_API_CACHE_ENABLED" to "false")
        assertTrue(client.networkInterceptors.none { it is CacheEnforcingInterceptor })
        assertTrue(client.interceptors.none { it is CacheHitLoggingInterceptor })
        assertNull(client.cache)
    }

    @Test
    fun `Increases read timeout`() {
        val client = buildClient()
        val defaultTimeout = OkHttpClient.Builder().build().readTimeoutMillis
        assertTrue(client.readTimeoutMillis > defaultTimeout)
    }

    private fun buildClient(
        vararg envVars: Pair<String, String?>,
        clientBuilder: OkHttpClient.Builder? = null,
    ): OkHttpClient {
        val fakeEnv = FakeEnv(*envVars)
        if ("GRADLE_ENTERPRISE_API_TOKEN" !in fakeEnv)
            fakeEnv["GRADLE_ENTERPRISE_API_TOKEN"] = "example-token"
        if ("GRADLE_ENTERPRISE_API_URL" !in fakeEnv)
            fakeEnv["GRADLE_ENTERPRISE_API_URL"] = "example-url"
        env = fakeEnv
        systemProperties = FakeSystemProperties.macOs
        keychain = FakeKeychain()
        val config = when (clientBuilder) {
            null -> Config()
            else -> Config(clientBuilder = clientBuilder)
        }
        return buildOkHttpClient(config)
    }
}
