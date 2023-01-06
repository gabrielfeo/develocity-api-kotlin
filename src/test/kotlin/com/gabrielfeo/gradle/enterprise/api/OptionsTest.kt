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
    fun `URL from env is required`() {
        val options = buildOptions(env = FakeEnv())
        assertFails {
            options.gradleEnterpriseInstance.url()
        }
    }

    @Test
    fun `Token from keychain is preferred`() {
        val options = buildOptions(
            keychain = FakeKeychain("gradle-enterprise-api-token" to "foo"),
            env = FakeEnv("GRADLE_ENTERPRISE_API_TOKEN" to "bar"),
        )
        assertEquals("foo", options.gradleEnterpriseInstance.token())
    }

    @Test
    fun `Token from env is fallback`() {
        val options = buildOptions(
            keychain = FakeKeychain(),
            env = FakeEnv("GRADLE_ENTERPRISE_API_TOKEN" to "bar"),
        )
        assertEquals("bar", options.gradleEnterpriseInstance.token())
    }

    @Test
    fun `Token from keychain or env is required`() {
        val options = buildOptions(
            keychain = FakeKeychain(),
            env = FakeEnv(),
        )
        assertFails {
            options.gradleEnterpriseInstance.token()
        }
    }

    @Test
    fun `maxConcurrentRequests accepts int`() {
        val options = buildOptions(
            keychain = FakeKeychain(),
            env = FakeEnv("GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS" to "1"),
        )
        assertDoesNotThrow {
            options.concurrency.maxConcurrentRequests
        }
    }

    @Test
    fun `default longTermCacheUrlPattern matches attributes path`() {
        val options = buildOptions()
        with(options.cache.longTermCacheUrlPattern) {
            assertTrue(matches("https://ge.gradle.org/api/builds/tgnsqkb2rhlni/gradle-attributes"))
            assertTrue(matches("https://ge.gradle.org/api/builds/tgnsqkb2rhlni/maven-attributes"))
        }
    }

    @Test
    fun `default shortTermCacheUrlPattern matches builds path`() {
        val options = buildOptions()
        with(options.cache.shortTermCacheUrlPattern) {
            assertTrue(matches("https://ge.gradle.org/api/builds?since=0"))
            assertTrue(matches("https://ge.gradle.org/api/builds?since=0&maxBuilds=2"))
        }
    }

    private fun buildOptions(
        env: Env = FakeEnv(),
    ): Options = buildOptions(env, FakeKeychain())
}