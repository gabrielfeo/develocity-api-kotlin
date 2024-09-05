package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.*
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.*

class ConfigTest {

    @BeforeTest
    fun before() {
        env = FakeEnv("DEVELOCITY_API_URL" to "https://example.com/api/")
        systemProperties = FakeSystemProperties()
    }

    @Test
    fun `Given no URL set in env, error`() {
        env = FakeEnv()
        assertFails {
            Config()
        }
    }

    @Test
    fun `Given URL set in env, apiUrl is env URL`() {
        (env as FakeEnv)["DEVELOCITY_API_URL"] = "https://example.com/api/"
        assertEquals("https://example.com/api/", Config().apiUrl)
    }

    @Test
    fun `Given no token, error`() {
        assertFails {
            Config().apiToken()
        }
    }

    @Test
    fun `Given token set in env, apiToken is env token`() {
        (env as FakeEnv)["DEVELOCITY_API_TOKEN"] = "bar"
        assertEquals("bar", Config().apiToken())
    }

    @Test
    fun `maxConcurrentRequests accepts int`() {
        (env as FakeEnv)["DEVELOCITY_API_MAX_CONCURRENT_REQUESTS"] = "1"
        assertDoesNotThrow {
            Config().maxConcurrentRequests
        }
    }

    @Test
    fun `Given timeout set in env, readTimeoutMillis returns env value`() {
        (env as FakeEnv)["DEVELOCITY_API_READ_TIMEOUT_MILLIS"] = "100000"
        assertEquals(100_000L, Config().readTimeoutMillis)
    }

    @Test
    fun `Given logLevel in env, logLevel is env value`() {
        (env as FakeEnv)["DEVELOCITY_API_LOG_LEVEL"] = "trace"
        assertEquals("trace", Config().logLevel)
    }

    @Test
    fun `Given logLevel in System props and not in env, logLevel is prop value`() {
        (env as FakeEnv)["DEVELOCITY_API_LOG_LEVEL"] = null
        (systemProperties as FakeSystemProperties).logLevel = "info"
        assertEquals("info", Config().logLevel)
    }

    @Test
    fun `Given no logLevel set, logLevel is off`() {
        (env as FakeEnv)["DEVELOCITY_API_LOG_LEVEL"] = null
        (systemProperties as FakeSystemProperties).logLevel = null
        assertEquals("off", Config().logLevel)
    }
}
