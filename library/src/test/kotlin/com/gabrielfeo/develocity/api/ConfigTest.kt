package com.gabrielfeo.develocity.api

import java.io.File
import com.gabrielfeo.develocity.api.internal.*
import com.gabrielfeo.develocity.api.internal.auth.AccessKeyResolver
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
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
    fun `Given no access key, error`() {
        assertFails {
            Config().accessKey()
        }
    }

    @Test
    fun `Given access key var with single host, accessKey is matching key`() {
        val host = java.net.URI(Config().apiUrl).host
        (env as FakeEnv)["DEVELOCITY_ACCESS_KEY"] = "$host=foo"
        assertEquals("foo", Config().accessKey())
    }

    @Test
    fun `Given access key var with multiple hosts, accessKey is matching key`() {
        val host = java.net.URI(Config().apiUrl).host
        (env as FakeEnv)["DEVELOCITY_ACCESS_KEY"] = "not$host=foo;$host=bar;alsonot$host=baz"
        assertEquals("bar", Config().accessKey())
    }

    @Test
    fun `Given access key var without matching host, error`() {
        val host = java.net.URI(Config().apiUrl).host
        (env as FakeEnv)["DEVELOCITY_ACCESS_KEY"] = "not$host=tokenvalue"
        assertFails {
            Config().accessKey()
        }
    }
    @Test
    fun `Given access key file with single host, accessKey is matching key`() {
        val host = java.net.URI(Config().apiUrl).host
        val fs = FakeFileSystem()
        val home = "/home/testuser".toPath()
        val keysPath = home / ".gradle/develocity/keys.properties"
        fs.createDirectories(keysPath.parent!!)
        fs.write(keysPath) { writeUtf8("$host=fromfile\n") }
        (env as FakeEnv)["DEVELOCITY_ACCESS_KEY"] = null
        val resolver = AccessKeyResolver(env, home, fs)
        assertEquals("fromfile", resolver.resolve(host))
    }

    @Test
    fun `Given access key file with multiple hosts, accessKey is matching key`() {
        val host = java.net.URI(Config().apiUrl).host
        val fs = FakeFileSystem()
        val home = "/home/testuser".toPath()
        val keysPath = home / ".gradle/develocity/keys.properties"
        fs.createDirectories(keysPath.parent!!)
        fs.write(keysPath) { writeUtf8("not$host=foo\n$host=fromfile\nother=bar\n") }
        (env as FakeEnv)["DEVELOCITY_ACCESS_KEY"] = null
        val resolver = AccessKeyResolver(env, home, fs)
        assertEquals("fromfile", resolver.resolve(host))
    }

    @Test
    fun `Given access key file without matching host, error`() {
        val host = java.net.URI(Config().apiUrl).host
        val fs = FakeFileSystem()
        val home = "/home/testuser".toPath()
        val keysPath = home / ".gradle/develocity/keys.properties"
        fs.createDirectories(keysPath.parent!!)
        fs.write(keysPath) { writeUtf8("not$host=foo\nother=bar\n") }
        (env as FakeEnv)["DEVELOCITY_ACCESS_KEY"] = null
        val resolver = AccessKeyResolver(env, home, fs)
        assertNull(resolver.resolve(host))
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
