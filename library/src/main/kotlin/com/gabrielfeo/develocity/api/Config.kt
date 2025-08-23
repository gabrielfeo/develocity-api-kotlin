package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.*
import com.gabrielfeo.develocity.api.internal.auth.*
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okio.FileSystem
import okio.Path.Companion.toPath
import java.io.File
import java.net.URI
import kotlin.time.Duration.Companion.days

/**
 * Library configuration options.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
data class Config(

    /**
     * Changes minimum log level for library classes, including the HTTP
     * client, **when using `slf4j-simple`** (bundled with the library). If
     * replacing SLF4J bindings, this setting has no effect, and log level
     * must be changed in the chosen logging framework.
     *
     * Default value, by order of precedence:
     *
     * - `DEVELOCITY_API_LOG_LEVEL` environment variable
     * - `org.slf4j.simpleLogger.defaultLogLevel` system property
     * - `"off"`
     *
     * SLF4J valid log levels and their usage by the library:
     *
     * - "off" (default, no logs)
     * - "error"
     * - "warn"
     * - "info"
     * - "debug" (logs HTTP traffic: URLs and status codes only)
     * - "trace" (logs HTTP traffic: full request and response including body, excluding
     *   authorization header)
     */
    val logLevel: String =
        env["DEVELOCITY_API_LOG_LEVEL"]
            ?: systemProperties.logLevel
            ?: "off",

    /**
     * Provides the URL of a Develocity API instance REST API. By default, uses
     * environment variable `DEVELOCITY_API_URL`. Must end with `/api/`.
     */
    val apiUrl: String =
        env["DEVELOCITY_API_URL"]
            ?.also { requireValidUrl(it) }
            ?: error(ERROR_NULL_API_URL),

    /**
     * Provides the access key for a Develocity API instance. By default, resolves to the first
     * key from these sources that matches the host of [apiUrl]:
     *
     * - variable `DEVELOCITY_ACCESS_KEY`
     * - variable `GRADLE_ENTERPRISE_ACCESS_KEY`
     * - file `$GRADLE_USER_HOME/.gradle/develocity/keys.properties` or, if `GRADLE_USER_HOME` is
     *   not set, `~/.gradle/develocity/keys.properties`
     * - file `~/.m2/.develocity/keys.properties`
     *
     * Refer to Develocity documentation for details on the format of such variables and files:
     *
     * - [Develocity Gradle Plugin User Manual][1]
     * - [Develocity Maven Extension User Manual][2]
     *
     * [1]: https://docs.gradle.com/develocity/gradle-plugin/current/#manual_access_key_configuration
     * [2]: https://docs.gradle.com/develocity/maven-extension/current/#manual_access_key_configuration
     *
     * @throws IllegalArgumentException if no matching key is found.
     */
    val accessKey: () -> String = {
        val host = URI(apiUrl).host
        requireNotNull(accessKeyResolver.resolve(host)) { ERROR_NULL_ACCESS_KEY }
    },

    /**
     * [OkHttpClient.Builder] to use when building the library's internal [OkHttpClient].
     *
     * This is aimed at using the library inside a full Kotlin project. Allows the internal client
     * to share resources such as thread pools with another [OkHttpClient]. See [OkHttpClient]
     * for all that is shared.
     *
     * The default is to share resources only within the library, i.e. multiple `Config()` with
     * the default [clientBuilder] will already share resources.
     */
    val clientBuilder: OkHttpClient.Builder = basicOkHttpClient.newBuilder(),

    /**
     * Maximum amount of concurrent requests allowed. Further requests will be queued. By default,
     * uses environment variable `DEVELOCITY_API_MAX_CONCURRENT_REQUESTS` or 5 (OkHttp's
     * default value of [Dispatcher.maxRequestsPerHost]).
     *
     * If set, will set [Dispatcher.maxRequests] and [Dispatcher.maxRequestsPerHost] of the
     * internal client, overwriting what's inherited from the base client of [clientBuilder],
     * if any.
     */
    val maxConcurrentRequests: Int? =
        env["DEVELOCITY_API_MAX_CONCURRENT_REQUESTS"]?.toInt(),

    /**
     * Timeout for reading an API response, used for [OkHttpClient.readTimeoutMillis].
     * By default, uses environment variable `DEVELOCITY_API_READ_TIMEOUT_MILLIS`
     * or 60_000. Keep in mind that Develocity API responses can be big and slow to send depending on
     * the endpoint.
     */
    val readTimeoutMillis: Long =
        env["DEVELOCITY_API_READ_TIMEOUT_MILLIS"]?.toLong()
            ?: 60_000L,

    /**
     * See [CacheConfig].
     */
    val cacheConfig: CacheConfig =
        CacheConfig(),
) {

    /**
     * HTTP cache is off by default, but can speed up requests significantly. The Develocity
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
     * Whenever Develocity is upgraded, cache should be [clear]ed.
     *
     * ### Caveats
     *
     * While not encouraged by the API, caching shouldn't have any major downsides other than a
     * time gap for certain queries, or having to reset cache when Develocity is upgraded.
     *
     * #### Time gap
     *
     * `/api/builds` responses always change as new builds are uploaded. Caching this path
     * short-term (default 1 day) means new builds uploaded after the cached response won't be
     * included in the query until the cache is invalidated 24h later. If that's a problem,
     * caching can be disabled for this `/api/builds` by changing [shortTermCacheUrlPattern].
     *
     * #### Develocity upgrades
     *
     * When Develocity is upgraded, any API response can change. New data might be available in API
     * endpoints such as `/api/build/{id}/gradle-attributes`. Thus, whenever the Develocity version
     * itself is upgraded, cache should be [clear]ed.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    data class CacheConfig(

        /**
         * Whether caching is enabled. By default, uses environment variable
         * `DEVELOCITY_API_CACHE_ENABLED` or `false`.
         */
        val cacheEnabled: Boolean =
            env["DEVELOCITY_API_CACHE_ENABLED"].toBoolean(),

        /**
         * HTTP cache location. By default, uses environment variable `DEVELOCITY_API_CACHE_DIR`
         * or creates a `~/.develocity-api-kotlin-cache` directory.
         */
        val cacheDir: File =
            env["DEVELOCITY_API_CACHE_DIR"]?.let(::File)
                ?: run {
                    val userHome = checkNotNull(systemProperties.userHome) { ERROR_NULL_USER_HOME }
                    File(userHome, ".develocity-api-kotlin-cache")
                },

        /**
         * Max size of the HTTP cache. By default, uses environment variable
         * `DEVELOCITY_API_MAX_CACHE_SIZE` or ~1 GB.
         */
        val maxCacheSize: Long = env["DEVELOCITY_API_MAX_CACHE_SIZE"]?.toLong()
            ?: 1_000_000_000L,

        /**
         * Regex pattern to match API URLs that are OK to store long-term in the HTTP cache, up to
         * [longTermCacheMaxAge] (1y by default, max value). By default, uses environment variable
         * `DEVELOCITY_API_LONG_TERM_CACHE_URL_PATTERN` or a pattern matching:
         * - {host}/api/builds/{id}/gradle-attributes
         * - {host}/api/builds/{id}/maven-attributes
         * - {host}/api/builds/{id}/gradle-build-cache-performance
         * - {host}/api/builds/{id}/maven-build-cache-performance
         *
         * Use `|` to define multiple patterns in one, e.g. `.*gradle-attributes|.*test-distribution`.
         */
        val longTermCacheUrlPattern: Regex =
            env["DEVELOCITY_API_LONG_TERM_CACHE_URL_PATTERN"]?.toRegex()
                ?: Regex(
                    """
                    .*/api/builds/[\d\w]+/(?:gradle|maven)-(?:attributes|build-cache-performance)
                """.trimIndent()
                ),

        /**
         * Max age in seconds for URLs to be cached long-term (matched by [longTermCacheUrlPattern]).
         * By default, uses environment variable `DEVELOCITY_API_LONG_TERM_CACHE_MAX_AGE` or 1 year.
         */
        val longTermCacheMaxAge: Long =
            env["DEVELOCITY_API_SHORT_TERM_CACHE_MAX_AGE"]?.toLong()
                ?: 365.days.inWholeSeconds,

        /**
         * Regex pattern to match API URLs that are OK to store short-term in the HTTP cache, up to
         * [shortTermCacheMaxAge] (1d by default). By default, uses environment variable
         * `DEVELOCITY_API_SHORT_TERM_CACHE_URL_PATTERN` or a pattern matching:
         * - {host}/api/builds
         *
         * Use `|` to define multiple patterns in one, e.g. `.*gradle-attributes|.*test-distribution`.
         */
        val shortTermCacheUrlPattern: Regex =
            env["DEVELOCITY_API_SHORT_TERM_CACHE_URL_PATTERN"]?.toRegex()
                ?: """.*/builds(?:\?.*|\Z)""".toRegex(),

        /**
         * Max age in seconds for URLs to be cached short-term (matched by [shortTermCacheUrlPattern]).
         * By default, uses environment variable `DEVELOCITY_API_SHORT_TERM_CACHE_MAX_AGE` or 1 day.
         */
        val shortTermCacheMaxAge: Long =
            env["DEVELOCITY_API_SHORT_TERM_CACHE_MAX_AGE"]?.toLong()
                ?: 1.days.inWholeSeconds,
    )
}

private fun requireValidUrl(string: String) {
    requireNotNull(runCatching { URI(string) }.getOrNull()) {
        ERROR_MALFORMED_API_URL.format(string)
    }
}

private const val ERROR_NULL_API_URL = "DEVELOCITY_API_URL is required"
private const val ERROR_MALFORMED_API_URL = "DEVELOCITY_API_URL contains a malformed URL: %s"
private const val ERROR_NULL_ACCESS_KEY = "Develocity access key not found. " +
    "Please set DEVELOCITY_ACCESS_KEY='[host]=[accessKey]' or see Config.accessKey javadoc for " +
    "other supported options."
private const val ERROR_NULL_USER_HOME = "'user.home' system property must not be null"
