package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.*
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.File
import java.util.logging.Logger
import kotlin.time.Duration.Companion.days

/**
 * Library configuration options.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
data class Options(

    /**
     * Enables debug logging from the library. All logging is output to stderr. By default, uses
     * environment variable `GRADLE_ENTERPRISE_API_DEBUG_LOGGING` or `false`.
     */
    val debugLoggingEnabled: Boolean =
        env["GRADLE_ENTERPRISE_API_DEBUG_LOGGING"].toBoolean(),

    /**
     * Provides the URL of a Gradle Enterprise API instance REST API. By default, uses
     * environment variable `GRADLE_ENTERPRISE_API_URL`. Must end with `/api/`.
     */
    val apiUrl: String =
        env["GRADLE_ENTERPRISE_API_URL"]
            ?: error("GRADLE_ENTERPRISE_API_URL is required"),

    /**
     * Provides the access token for a Gradle Enterprise API instance. By default, uses keychain entry
     * `gradle-enterprise-api-token` or environment variable `GRADLE_ENTERPRISE_API_TOKEN`.
     */
    val apiToken: () -> String = {
        requireEnvOrKeychainToken(debugLoggingEnabled = debugLoggingEnabled)
    },

    /**
     * Provider of an [OkHttpClient.Builder] to use when building the library's internal client.
     * Has a default value and shouldn't be needed in scripts.
     *
     * This is aimed at using the library inside a full Kotlin project. Allows the internal client to
     * share resources such as thread pools with another [OkHttpClient], useful for full Kotlin projects
     * and rarely needed for scripting. See [OkHttpClient] for all that is shared.
     */
    val clientBuilder: OkHttpClient.Builder =
        OkHttpClient.Builder(),

    /**
     * Maximum amount of concurrent requests allowed. Further requests will be queued. By default,
     * uses environment variable `GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS` or 5 (OkHttp's
     * default value of [Dispatcher.maxRequestsPerHost]).
     *
     * If set, will set [Dispatcher.maxRequests] and [Dispatcher.maxRequestsPerHost] of the
     * internal client, overwriting what's inherited from the base client of [clientBuilder],
     * if any.
     */
    val maxConcurrentRequests: Int? =
        env["GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS"]?.toInt(),

    /**
     * Timeout for reading an API response, used for [OkHttpClient.readTimeoutMillis].
     * By default, uses environment variable `GRADLE_ENTERPRISE_API_READ_TIMEOUT_MILLIS`
     * or 60_000. Keep in mind that GE API responses can be big and slow to send depending on
     * the endpoint.
     */
    val readTimeoutMillis: Long =
        env["GRADLE_ENTERPRISE_API_READ_TIMEOUT_MILLIS"]?.toLong()
            ?: 60_000L,

    /**
     * See [CacheOptions].
     */
    val cacheOptions: CacheOptions =
        CacheOptions(),
) {

    /**
     * HTTP cache is off by default, but can speed up requests significantly. The Gradle Enterprise
     * API disallows HTTP caching, but this library forcefully enables it by overwriting
     * cache-related headers in API responses. Enable with [cacheEnabled].
     *
     * Responses can be:
     *
     * - cached short-term: default max-age of 1 day
     *   - `/api/builds`
     * - cached long-term: default max-age of 1 year
     *   - `/api/builds/{id}/gradle-attributes`
     *   - `/api/builds/{id}/maven-attributes`
     *   - `/api/builds/{id}/gradle-build-cache-performance`
     *   - `/api/builds/{id}/maven-build-cache-performance`
     * - not cached
     *   - all other paths
     *
     * Whether a response is cached short-term, long-term or not cached at
     * all depends on whether it was matched by [shortTermCacheUrlPattern] or
     * [longTermCacheUrlPattern].
     *
     * Whenever GE is upgraded, cache should be [clear]ed.
     *
     * ### Caveats
     *
     * While not encouraged by the API, caching shouldn't have any major downsides other than a
     * time gap for certain queries, or having to reset cache when GE is upgraded.
     *
     * #### Time gap
     *
     * `/api/builds` responses always change as new builds are uploaded. Caching this path
     * short-term (default 1 day) means new builds uploaded after the cached response won't be
     * included in the query until the cache is invalidated 24h later. If that's a problem,
     * caching can be disabled for this `/api/builds` by changing [shortTermCacheUrlPattern].
     *
     * #### GE upgrades
     *
     * When GE is upgraded, any API response can change. New data might be available in API
     * endpoints such as `/api/build/{id}/gradle-attributes`. Thus, whenever the GE version
     * itself is upgraded, cache should be [clear]ed.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    data class CacheOptions(

        /**
         * Whether caching is enabled. By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_CACHE_ENABLED` or `false`.
         */
        val cacheEnabled: Boolean =
            env["GRADLE_ENTERPRISE_API_CACHE_ENABLED"].toBoolean(),

        /**
         * HTTP cache location. By default, uses environment variable `GRADLE_ENTERPRISE_API_CACHE_DIR`
         * or the system temporary folder (`java.io.tmpdir` / gradle-enterprise-api-kotlin-cache).
         */
        val cacheDir: File =
            env["GRADLE_ENTERPRISE_API_CACHE_DIR"]?.let(::File)
                ?: File(System.getProperty("java.io.tmpdir"), "gradle-enterprise-api-kotlin-cache"),

        /**
         * Max size of the HTTP cache. By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_MAX_CACHE_SIZE` or ~1 GB.
         */
        val maxCacheSize: Long = env["GRADLE_ENTERPRISE_API_MAX_CACHE_SIZE"]?.toLong()
            ?: 1_000_000_000L,

        /**
         * Regex pattern to match API URLs that are OK to store long-term in the HTTP cache, up to
         * [longTermCacheMaxAge] (1y by default, max value). By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_URL_PATTERN` or a pattern matching:
         * - {host}/api/builds/{id}/gradle-attributes
         * - {host}/api/builds/{id}/maven-attributes
         * - {host}/api/builds/{id}/gradle-build-cache-performance
         * - {host}/api/builds/{id}/maven-build-cache-performance
         *
         * Use `|` to define multiple patterns in one, e.g. `.*gradle-attributes|.*test-distribution`.
         */
        val longTermCacheUrlPattern: Regex =
            env["GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_URL_PATTERN"]?.toRegex()
                ?: Regex(
                    """
                    .*/api/builds/[\d\w]+/(?:gradle|maven)-(?:attributes|build-cache-performance)
                """.trimIndent()
                ),

        /**
         * Max age in seconds for URLs to be cached long-term (matched by [longTermCacheUrlPattern]).
         * By default, uses environment variable `GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_MAX_AGE` or 1 year.
         */
        val longTermCacheMaxAge: Long =
            env["GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE"]?.toLong()
                ?: 365.days.inWholeSeconds,

        /**
         * Regex pattern to match API URLs that are OK to store short-term in the HTTP cache, up to
         * [shortTermCacheMaxAge] (1d by default). By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_URL_PATTERN` or a pattern matching:
         * - {host}/api/builds
         *
         * Use `|` to define multiple patterns in one, e.g. `.*gradle-attributes|.*test-distribution`.
         */
        val shortTermCacheUrlPattern: Regex =
            env["GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_URL_PATTERN"]?.toRegex()
                ?: """.*/builds(?:\?.*|\Z)""".toRegex(),

        /**
         * Max age in seconds for URLs to be cached short-term (matched by [shortTermCacheUrlPattern]).
         * By default, uses environment variable `GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE` or 1 day.
         */
        val shortTermCacheMaxAge: Long =
            env["GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE"]?.toLong()
                ?: 1.days.inWholeSeconds,
    )
}

internal fun requireEnvOrKeychainToken(debugLoggingEnabled: Boolean): String {
    if (systemProperties["os.name"] == "Mac OS X") {
        when (val result = keychain.get("gradle-enterprise-api-token")) {
            is KeychainResult.Success -> return result.token
            is KeychainResult.Error -> {
                if (debugLoggingEnabled) {
                    val logger = Logger.getGlobal()
                    logger.info("Failed to get key from keychain (${result.description})")
                }
            }
        }
    }
    return env["GRADLE_ENTERPRISE_API_TOKEN"]
        ?: error("GRADLE_ENTERPRISE_API_TOKEN is required")
}
