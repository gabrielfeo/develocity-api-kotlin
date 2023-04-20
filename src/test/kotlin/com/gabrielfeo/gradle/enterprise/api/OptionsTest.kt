package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.Env
import com.gabrielfeo.gradle.enterprise.api.internal.FakeEnv
import com.gabrielfeo.gradle.enterprise.api.internal.FakeKeychain
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class OptionsTest {

    @Test
    fun `Given no URL set in env, url() fails`() {
        val options = Options(FakeEnv(), FakeKeychain())
        assertFails {
            options.gradleEnterpriseInstance.url()
        }
    }

    @Test
    fun `Given URL set in env, url() returns env URL`() {
        val options = Options(
            FakeEnv("GRADLE_ENTERPRISE_API_URL" to "https://example.com/api/"),
            FakeKeychain(),
        )
        assertEquals("https://example.com/api/", options.gradleEnterpriseInstance.url())
    }

    @Test
    fun `Token from keychain is preferred`() {
        val options = Options(
            keychain = FakeKeychain("gradle-enterprise-api-token" to "foo"),
            env = FakeEnv("GRADLE_ENTERPRISE_API_TOKEN" to "bar"),
        )
        assertEquals("foo", options.gradleEnterpriseInstance.token())
    }

    @Test
    fun `Token from env is fallback`() {
        val options = Options(
            keychain = FakeKeychain(),
            env = FakeEnv("GRADLE_ENTERPRISE_API_TOKEN" to "bar"),
        )
        assertEquals("bar", options.gradleEnterpriseInstance.token())
    }

    @Test
    fun `Token from keychain or env is required`() {
        val options = Options(
            keychain = FakeKeychain(),
            env = FakeEnv(),
        )
        assertFails {
            options.gradleEnterpriseInstance.token()
        }
    }

    @Test
    fun `maxConcurrentRequests accepts int`() {
        val options = Options(
            keychain = FakeKeychain(),
            env = FakeEnv("GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS" to "1"),
        )
        assertDoesNotThrow {
            options.httpClient.maxConcurrentRequests
        }
    }

    @Test
    fun `default longTermCacheUrlPattern matches attributes URLs`() {
        val options = Options(FakeEnv(), FakeKeychain())
        options.cache.longTermCacheUrlPattern.assertMatches(
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/gradle-attributes",
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/maven-attributes",
        )
    }

    @Test
    fun `default longTermCacheUrlPattern matches build cache performance URLs`() {
        val options = Options(FakeEnv(), FakeKeychain())
        options.cache.longTermCacheUrlPattern.assertMatches(
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/gradle-build-cache-performance",
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/maven-build-cache-performance",
        )
    }

    @Test
    fun `default shortTermCacheUrlPattern matches builds URLs`() {
        val options = Options(FakeEnv(), FakeKeychain())
        options.cache.shortTermCacheUrlPattern.assertMatches(
            "https://ge.gradle.org/api/builds?since=0",
            "https://ge.gradle.org/api/builds?since=0&maxBuilds=2",
        )
    }

    @Test
    fun `Given timeout set in env, readTimeoutMillis returns env value`() {
        val options = Options(
            FakeEnv("GRADLE_ENTERPRISE_API_READ_TIMEOUT_MILLIS" to "100000"),
            FakeKeychain(),
        )
        assertEquals(100_000L, options.httpClient.readTimeoutMillis)
    }

    private fun Regex.assertMatches(vararg values: String) {
        values.forEach {
            assertTrue(matches(it), "/$pattern/ doesn't match '$it'")
        }
    }
}