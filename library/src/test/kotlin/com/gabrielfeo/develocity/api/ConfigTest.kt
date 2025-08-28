package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.*
import com.gabrielfeo.develocity.api.internal.auth.*
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import java.net.URI
import kotlin.test.*

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
        assertEquals(URI("https://example.com/"), Config().server)
    }

    @Test
    fun `Given server URL set in code, server is correct URL`() {
        val config = Config(server = URI("https://custom.example.com/"))
        assertEquals(URI("https://custom.example.com/"), config.server)
    }

    @TestFactory
    fun `Given malformed URL, error`() = listOf(
        "mailto:foo@bar.com",
        "file:///example/foo",
        "http://example.com?foo",
        "https://example.com?foo",
        "https://example.com/foo",
        "https://example.com/foo?bar=1",
        "https://example.com/foo/bar/baz",
        "https://example.com/foo/bar/baz?qux=1",
    ).map { url ->
        dynamicTest(url) {
            assertFailsWith<IllegalArgumentException> {
                Config(server = URI(url))
            }
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
