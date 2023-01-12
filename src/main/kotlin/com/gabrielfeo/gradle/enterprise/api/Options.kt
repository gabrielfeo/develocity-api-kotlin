@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.*
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.File
import kotlin.time.Duration.Companion.days

/**
 * The global [Options] instance.
 */
val options = Options(env = RealEnv, keychain = RealKeychain(RealEnv))

/**
 * Library configuration options. Should not be changed after accessing the [gradleEnterpriseApi]
 * object for the first time.
 *
 * Use the global [options] instance.
 */
class Options internal constructor(
    env: Env,
    keychain: Keychain,
) {

    val gradleEnterpriseInstance = GradleEnterpriseInstanceOptions(env, keychain)
    val httpClient = HttpClientOptions(env)
    val cache = CacheOptions(env)
    val debugging = DebuggingOptions(env)

    /**
     * Options about the GE instance, such as URL and API token.
     *
     * Access via the global [options] instance: `options.gradleEnterpriseInstance`.
     */
    class GradleEnterpriseInstanceOptions internal constructor(
        private val env: Env,
        private val keychain: Keychain,
    ) {

        /**
         * Provides the URL of a Gradle Enterprise API instance REST API. By default, uses
         * environment variable `GRADLE_ENTERPRISE_API_URL`. Must end with `/api/`.
         */
        var url: () -> String = {
            env["GRADLE_ENTERPRISE_API_URL"]
                ?: error("GRADLE_ENTERPRISE_API_URL is required")
        }

        /**
         * Provides the access token for a Gradle Enterprise API instance. By default, uses keychain entry
         * `gradle-enterprise-api-token` or environment variable `GRADLE_ENTERPRISE_API_TOKEN`.
         */
        var token: () -> String = {
            keychain["gradle-enterprise-api-token"]
                ?: env["GRADLE_ENTERPRISE_API_TOKEN"]
                ?: error("GRADLE_ENTERPRISE_API_TOKEN is required")
        }
    }

    /**
     * HTTP client options.
     *
     * Access via the global [options] instance: `options.httpClient`.
     */
    class HttpClientOptions internal constructor(
        env: Env,
    ) {

        /**
         * Provider of an [OkHttpClient.Builder] to use when building the library's internal client.
         * Has a default value and shouldn't be needed in scripts.
         *
         * This is aimed at using the library inside a full Kotlin project. Allows the internal client to
         * share resources such as thread pools with another [OkHttpClient], useful for full Kotlin projects
         * and rarely needed for scripting. See [OkHttpClient] for all that is shared.
         */
        var clientBuilder: () -> OkHttpClient.Builder = {
            OkHttpClient.Builder()
        }

        /**
         * Maximum amount of concurrent requests allowed. Further requests will be queued. By default,
         * uses environment variable `GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS` or 5 (OkHttp's
         * default value of [Dispatcher.maxRequestsPerHost]).
         *
         * If set, will set [Dispatcher.maxRequests] and [Dispatcher.maxRequestsPerHost] of the
         * internal client, overwriting what's inherited from the base client of [clientBuilder],
         * if any.
         */
        var maxConcurrentRequests =
            env["GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS"]?.toInt()
    }

    /**
     * HTTP cache is off by default, but can speed up requests significantly. The Gradle Enterprise
     * API disallows HTTP caching, but this library forcefully enables it by overwriting
     * cache-related headers in API responses. Enable with [cacheEnabled].
     *
     * Access via the global [options] instance: `options.cache`.
     *
     * Responses can be:
     *
     * - cached short-term: default max-age of 1 day
     *   - `/api/builds`
     * - cached long-term: default max-age of 1 year
     *   - `/api/builds/{id}/gradle-attributes`
     *   - `/api/builds/{id}/maven-attributes`
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
    class CacheOptions internal constructor(
        env: Env,
    ) {

        /**
         * Whether caching is enabled. By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_CACHE_ENABLED` or `false`.
         */
        var cacheEnabled: Boolean =
            env["GRADLE_ENTERPRISE_API_CACHE_ENABLED"].toBoolean()

        /**
         * Clears [cacheDir] including files that weren't created by the cache.
         */
        fun clear() {
            buildCache(options).delete()
        }

        /**
         * HTTP cache location. By default, uses environment variable `GRADLE_ENTERPRISE_API_CACHE_DIR`
         * or the system temporary folder (`java.io.tmpdir` / gradle-enterprise-api-kotlin-cache).
         */
        var cacheDir =
            env["GRADLE_ENTERPRISE_API_CACHE_DIR"]?.let(::File)
                ?: File(System.getProperty("java.io.tmpdir"), "gradle-enterprise-api-kotlin-cache")

        /**
         * Max size of the HTTP cache. By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_MAX_CACHE_SIZE` or ~1 GB.
         */
        var maxCacheSize =
            env["GRADLE_ENTERPRISE_API_MAX_CACHE_SIZE"]?.toLong()
                ?: 1_000_000_000L

        /**
         * Regex pattern to match API URLs that are OK to store long-term in the HTTP cache, up to
         * [longTermCacheMaxAge] (1y by default, max value). By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_URL_PATTERN` or a pattern matching:
         * - {host}/api/builds/{id}/gradle-attributes
         * - {host}/api/builds/{id}/maven-attributes
         *
         * Use `|` to define multiple patterns in one, e.g. `.*gradle-attributes|.*test-distribution`.
         */
        var longTermCacheUrlPattern: Regex =
            env["GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_URL_PATTERN"]?.toRegex()
                ?: """.*/api/builds/[\d\w]+/(?:gradle|maven)-attributes""".toRegex()

        /**
         * Max age in seconds for URLs to be cached long-term (matched by [longTermCacheUrlPattern]).
         * By default, uses environment variable `GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_MAX_AGE` or 1 year.
         */
        var longTermCacheMaxAge: Long =
            env["GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE"]?.toLong()
                ?: 365.days.inWholeSeconds

        /**
         * Regex pattern to match API URLs that are OK to store short-term in the HTTP cache, up to
         * [shortTermCacheMaxAge] (1d by default). By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_URL_PATTERN` or a pattern matching:
         * - {host}/api/builds
         *
         * Use `|` to define multiple patterns in one, e.g. `.*gradle-attributes|.*test-distribution`.
         */
        var shortTermCacheUrlPattern: Regex =
            env["GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_URL_PATTERN"]?.toRegex()
                ?: """.*/builds(?:\?.*|\Z)""".toRegex()

        /**
         * Max age in seconds for URLs to be cached short-term (matched by [shortTermCacheUrlPattern]).
         * By default, uses environment variable `GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE` or 1 day.
         */
        var shortTermCacheMaxAge: Long =
            env["GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE"]?.toLong()
                ?: 1.days.inWholeSeconds
    }

    /**
     * Library debugging options.
     *
     * Access via the global [options] instance: `options.debugging`.
     */
    class DebuggingOptions internal constructor(
        env: Env,
    ) {

        /**
         * Enables debug logging from the library. All logging is output to stderr. By default, uses
         * environment variable `GRADLE_ENTERPRISE_API_DEBUG_LOGGING` or `false`.
         */
        var debugLoggingEnabled =
            env["GRADLE_ENTERPRISE_API_DEBUG_LOGGING"].toBoolean()
    }
}
