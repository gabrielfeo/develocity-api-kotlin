package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.*
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.*

class CacheOptionsTest {

    @BeforeTest
    fun before() {
        env = FakeEnv("GRADLE_ENTERPRISE_API_URL" to "https://example.com/api/")
        systemProperties = FakeSystemProperties.macOs
        keychain = FakeKeychain()
    }

    @Test
    fun `default longTermCacheUrlPattern matches attributes URLs`() {
        Options.CacheOptions().longTermCacheUrlPattern.assertMatches(
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/gradle-attributes",
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/maven-attributes",
        )
    }

    @Test
    fun `default longTermCacheUrlPattern matches build cache performance URLs`() {
        Options.CacheOptions().longTermCacheUrlPattern.assertMatches(
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/gradle-build-cache-performance",
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/maven-build-cache-performance",
        )
    }

    @Test
    fun `default shortTermCacheUrlPattern matches builds URLs`() {
        Options.CacheOptions().shortTermCacheUrlPattern.assertMatches(
            "https://ge.gradle.org/api/builds?since=0",
            "https://ge.gradle.org/api/builds?since=0&maxBuilds=2",
        )
    }

    private fun Regex.assertMatches(vararg values: String) {
        values.forEach {
            assertTrue(matches(it), "/$pattern/ doesn't match '$it'")
        }
    }
}