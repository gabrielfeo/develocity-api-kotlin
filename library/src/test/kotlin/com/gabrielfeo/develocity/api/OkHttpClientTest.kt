package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.*
import com.gabrielfeo.develocity.api.internal.auth.AccessKeyResolver
import com.gabrielfeo.develocity.api.internal.auth.HttpBearerAuth
import com.gabrielfeo.develocity.api.internal.auth.accessKeyResolver
import com.gabrielfeo.develocity.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.develocity.api.internal.caching.CacheHitLoggingInterceptor
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
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
            "DEVELOCITY_API_MAX_CONCURRENT_REQUESTS" to "123"
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
    fun `Given cache enabled, configures caching`() {
        val client = buildClient("DEVELOCITY_API_CACHE_ENABLED" to "true")
        assertTrue(client.networkInterceptors.any { it is CacheEnforcingInterceptor })
        assertNotNull(client.cache)
    }

    @Test
    fun `Given cache disabled, no caching or cache logging`() {
        val client = buildClient("DEVELOCITY_API_CACHE_ENABLED" to "false")
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

    @Test
    fun `Logs under library package`() {
        val loggerFactory = ProxyLoggerFactory(delegate = RealLoggerFactory(Config()))
        buildClient(loggerFactory = loggerFactory)
        loggerFactory.createdLoggers.let {
            assertTrue(it.isNotEmpty())
            it.forEach {
                assertTrue(
                    it.name.startsWith("com.gabrielfeo.develocity.api"),
                    "Logger name '${it.name}' should start with 'com.gabrielfeo.develocity.api'"
                )
            }
        }
    }

    private fun buildClient(
        vararg envVars: Pair<String, String?>,
        clientBuilder: OkHttpClient.Builder? = null,
        loggerFactory: LoggerFactory? = null,
    ): OkHttpClient {
        val fakeEnv = FakeEnv(*envVars)
        if ("DEVELOCITY_ACCESS_KEY" !in fakeEnv)
            fakeEnv["DEVELOCITY_ACCESS_KEY"] = "example.com=example-token"
        if ("DEVELOCITY_URL" !in fakeEnv)
            fakeEnv["DEVELOCITY_URL"] = "https://example.com/"
        env = fakeEnv
        systemProperties = FakeSystemProperties()
        accessKeyResolver = AccessKeyResolver(
            env,
            homeDirectory = "/home/testuser".toPath(),
            fileSystem = FakeFileSystem(),
        )
        val config = when (clientBuilder) {
            null -> Config()
            else -> Config(clientBuilder = clientBuilder)
        }
        return buildOkHttpClient(config, loggerFactory ?: RealLoggerFactory(config))
    }
}
