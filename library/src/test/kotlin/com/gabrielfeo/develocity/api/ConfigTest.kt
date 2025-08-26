package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.FakeEnv
import com.gabrielfeo.develocity.api.internal.FakeSystemProperties
import com.gabrielfeo.develocity.api.internal.auth.AccessKeyResolver
import com.gabrielfeo.develocity.api.internal.auth.accessKeyResolver
import com.gabrielfeo.develocity.api.internal.env
import com.gabrielfeo.develocity.api.internal.systemProperties
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.assertDoesNotThrow
import java.net.URL
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ConfigTest {

    @BeforeTest
    fun before() {
        env = FakeEnv("DEVELOCITY_URL" to "https://example.com/")
        systemProperties = FakeSystemProperties()
        accessKeyResolver = AccessKeyResolver(
            env,
            homeDirectory = "/home/testuser".toPath(),
            fileSystem = FakeFileSystem(),
        )
    }

    @Test
    fun `Given no URL set in env, error`() {
        env = FakeEnv()
        assertFails {
            Config()
        }
    }

    @Test
    fun `Given server URL set in env, server is correct URL`() {
        (env as FakeEnv)["DEVELOCITY_URL"] = "https://example.com/"
        assertEquals(URL("https://example.com/"), Config().server)
    }

    @Test
    fun `Given server URL set in code, server is correct URL`() {
        val config = Config(server = URL("https://custom.example.com/"))
        assertEquals(URL("https://custom.example.com/"), config.server)
    }

    @Test
    fun `Given URL with path, error`() {
        assertFails {
            Config(server = URL("https://example.com/foo"))
        }
    }

    @Test
    fun `Given URL with query, error`() {
        assertFails {
            Config(server = URL("https://example.com?foo=bar"))
        }
    }

    @Test
    fun `Given invalid URL, error`() {
        assertFails {
            Config(server = URL("https:/example.com&"))
        }
    }

    @Test
    fun `Given default access key function and resolvable key, accessKey is key`() {
        (env as FakeEnv)["DEVELOCITY_URL"] = "https://example.com/"
        (env as FakeEnv)["DEVELOCITY_ACCESS_KEY"] = "example.com=foo"
        assertEquals("foo", Config().accessKey())
    }

    @Test
    fun `Given default access key and no resolvable key, error`() {
        (env as FakeEnv)["DEVELOCITY_URL"] = "https://example.com/"
        (env as FakeEnv)["DEVELOCITY_ACCESS_KEY"] = "notexample.com=foo"
        assertFails {
            Config().accessKey()
        }
    }

    @Test
    fun `Given custom access key function fails, error`() {
        assertFails {
            Config(accessKey = { error("foo") }).accessKey()
        }
    }

    @Test
    fun `Given custom access key function yields value, accessKey is value`() {
        assertEquals("foo", Config(accessKey = { "foo" }).accessKey())
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
