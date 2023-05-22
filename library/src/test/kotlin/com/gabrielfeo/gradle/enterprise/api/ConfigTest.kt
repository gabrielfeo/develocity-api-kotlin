package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.*
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.*

class ConfigTest {

    @BeforeTest
    fun before() {
        env = FakeEnv("GRADLE_ENTERPRISE_API_URL" to "https://example.com/api/")
        systemProperties = FakeSystemProperties.macOs
        keychain = FakeKeychain()
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
        (env as FakeEnv)["GRADLE_ENTERPRISE_API_URL"] = "https://example.com/api/"
        assertEquals("https://example.com/api/", Config().apiUrl)
    }

    @Test
    fun `Given macOS and keychain token, keychain token used`() {
        (env as FakeEnv)["GRADLE_ENTERPRISE_API_TOKEN"] = "bar"
        keychain = FakeKeychain("gradle-enterprise-api-token" to "foo")
        assertEquals("foo", Config().apiToken())
    }

    @Test
    fun `Given macOS but no keychain token, env token used`() {
        (env as FakeEnv)["GRADLE_ENTERPRISE_API_TOKEN"] = "bar"
        assertEquals("bar", Config().apiToken())
    }

    @Test
    fun `Given Linux, keychain never tried and env token used`() {
        (env as FakeEnv)["GRADLE_ENTERPRISE_API_TOKEN"] = "bar"
        keychain = object : Keychain {
            override fun get(entry: String) =
                error("Error: Tried to access macOS keychain in Linux")
        }
        systemProperties = FakeSystemProperties.linux
        assertEquals("bar", Config().apiToken())
    }

    @Test
    fun `Given macOS and no token anywhere, error`() {
        assertFails {
            Config().apiToken()
        }
    }

    @Test
    fun `Given Linux and no env token, fails`() {
        systemProperties = FakeSystemProperties.linux
        assertFails {
            Config().apiToken()
        }
    }

    @Test
    fun `maxConcurrentRequests accepts int`() {
        (env as FakeEnv)["GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS"] = "1"
        assertDoesNotThrow {
            Config().maxConcurrentRequests
        }
    }

    @Test
    fun `Given timeout set in env, readTimeoutMillis returns env value`() {
        (env as FakeEnv)["GRADLE_ENTERPRISE_API_READ_TIMEOUT_MILLIS"] = "100000"
        assertEquals(100_000L, Config().readTimeoutMillis)
    }
}