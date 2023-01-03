package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.requireBaseUrl
import com.gabrielfeo.gradle.enterprise.api.internal.requireToken
import java.io.File
import kotlin.time.Duration.Companion.days

/**
 * Library configuration options. Should not be changed after accessing the [api] object for the
 * first time.
 */
object Options {

    /**
     * Provides the URL of a Gradle Enterprise API instance. By default, uses environment variable
     * `GRADLE_ENTERPRISE_URL`.
     */
    var baseUrl: () -> String = {
        requireBaseUrl(envName = "GRADLE_ENTERPRISE_URL")
    }

    /**
     * Provides the access token for a Gradle Enterprise API instance. By default, uses keychain entry
     * `gradle-enterprise-api-token` or environment variable `GRADLE_ENTERPRISE_URL`.
     */
    var accessToken: () -> String = {
        requireToken(
            keychainName = "gradle-enterprise-api-token",
            envName = "GRADLE_ENTERPRISE_API_TOKEN",
        )
    }

    /**
     * Maximum amount of concurrent requests allowed. Further requests will be queued. By default,
     * uses environment variable `GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS` or 15.
     *
     * https://square.github.io/okhttp/4.x/okhttp/okhttp3/-dispatcher
     */
    var maxConcurrentRequests =
        System.getenv("GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS")?.toInt()
            ?: 15

    /**
     * HTTP cache location. By default, uses environment variable `GRADLE_ENTERPRISE_API_CACHE_DIR`
     * or the system temporary folder (`java.io.tmpdir` / gradle-enterprise-api-kotlin-cache).
     */
    var cacheDir =
        System.getenv("GRADLE_ENTERPRISE_API_CACHE_DIR")?.let(::File)
            ?: File(System.getProperty("java.io.tmpdir"), "gradle-enterprise-api-kotlin-cache")

    /**
     * Max size of the HTTP cache. By default, uses environment variable
     * `GRADLE_ENTERPRISE_API_MAX_CACHE_SIZE` or ~1 GB.
     */
    var maxCacheSize =
        System.getenv("GRADLE_ENTERPRISE_API_MAX_CACHE_SIZE")?.toLong()
            ?: 1_000_000_000L

    /**
     * Regex pattern to match API URLs that are OK to store long-term in the HTTP cache, up to
     * [longTermCacheMaxAge] (1y by default, max value). By default, uses environment variable
     * `GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_URL_PATTERN` or a pattern matching:
     * - {host}/api/builds/{id}/gradle-attributes
     * - {host}/api/builds/{id}/maven-attributes
     *
     * Gradle Enterprise API disallows HTTP caching, but this library forcefully removes such
     * restriction.
     *
     * Use `|` to define multiple patterns in one, e.g. `.*gradle-attributes|.*test-distribution`.
     */
    var longTermCacheUrlPattern: Regex =
        System.getenv("GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_URL_PATTERN")?.toRegex()
            ?: """.*/api/builds/[\d\w]+/(?:gradle|maven)-attributes""".toRegex()

    /**
     * Max age in seconds for URLs to be cached long-term (matched by [longTermCacheUrlPattern]).
     * By default, uses environment variable `GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_MAX_AGE` or 1 year.
     */
    var longTermCacheMaxAge: Long =
        System.getenv("GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE")?.toLong()
            ?: 365.days.inWholeSeconds

    /**
     * Regex pattern to match API URLs that are OK to store short-term in the HTTP cache, up to
     * [shortTermCacheMaxAge] (1d by default). By default, uses environment variable
     * `GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_URL_PATTERN` or a pattern matching:
     * - {host}/api/builds
     *
     * Gradle Enterprise API disallows HTTP caching, but this library forcefully removes such
     * restriction.
     *
     * Use `|` to define multiple patterns in one, e.g. `.*gradle-attributes|.*test-distribution`.
     */
    var shortTermCacheUrlPattern: Regex =
        System.getenv("GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_URL_PATTERN")?.toRegex()
            ?: """.*/builds(?:\?.*|\Z)""".toRegex()

    /**
     * Max age in seconds for URLs to be cached short-term (matched by [shortTermCacheUrlPattern]).
     * By default, uses environment variable `GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE` or 1 day.
     */
    var shortTermCacheMaxAge: Long =
        System.getenv("GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE")?.toLong()
            ?: 1.days.inWholeSeconds

    /**
     * Enables debug logging from the library. All logging is output to stderr. By default, uses
     * environment variable `GRADLE_ENTERPRISE_API_DEBUG_LOGGING` or `false`.
     */
    var debugLoggingEnabled =
        System.getenv("GRADLE_ENTERPRISE_API_DEBUG_LOGGING").toBoolean()
}