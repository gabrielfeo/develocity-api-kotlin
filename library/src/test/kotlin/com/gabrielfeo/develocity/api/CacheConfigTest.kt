package com.gabrielfeo.develocity.api

import kotlin.test.*

class CacheConfigTest {

    @Test
    fun `default longTermCacheUrlPattern matches attributes URLs`() {
        Config.CacheConfig().longTermCacheUrlPattern.assertMatches(
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/gradle-attributes",
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/maven-attributes",
        )
    }

    @Test
    fun `default longTermCacheUrlPattern matches build cache performance URLs`() {
        Config.CacheConfig().longTermCacheUrlPattern.assertMatches(
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/gradle-build-cache-performance",
            "https://ge.gradle.org/api/builds/tgnsqkb2rhlni/maven-build-cache-performance",
        )
    }

    @Test
    fun `default shortTermCacheUrlPattern matches builds URLs`() {
        Config.CacheConfig().shortTermCacheUrlPattern.assertMatches(
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
