@file:Suppress("unused")

package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.*
import java.io.File

/**
 * The global instance of [GradleEnterpriseApi].
 */
val api: GradleEnterpriseApi by lazy {
    retrofit.create(GradleEnterpriseApi::class.java)
}

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
 * Shutdown the internal OkHttp client, releasing resources and allowing the program to finish
 * before the client's idle timeout.
 *
 * https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/#shutdown-isnt-necessary
 */
fun shutdown() {
    okHttpClient.dispatcher.executorService.shutdownNow()
}

/**
 * Regex pattern to match API URLs that are OK to store in the HTTP cache. Matches by default:
 * - {host}/api/builds/{id}/gradle-attributes
 * - {host}/api/builds/{id}/maven-attributes
 *
 * By default, the Gradle Enterprise API disallows HTTP caching via response headers. This library
 * removes such headers to forcefully allow caching, if the path is matched by any of these
 * patterns.
 *
 * Use `|` to define multiple patterns in one, e.g. `.*gradle-attributes|.*test-distribution`.
 */
val cacheableUrlPattern: Regex = System.getenv("GRADLE_ENTERPRISE_API_CACHEABLE_URL_PATTERN")
    ?.toRegex()
    ?: """.*/api/builds/[\d\w]+/(?:gradle|maven)-attributes""".toRegex()

/**
 * Maximum amount of concurrent requests allowed. Further requests will be queued. By default,
 * uses environment variable `GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS` or 15.
 *
 * https://square.github.io/okhttp/4.x/okhttp/okhttp3/-dispatcher
 */
var maxConcurrentRequests = System.getenv("GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS")?.toInt()
    ?: 15

/**
 * Max size of the HTTP cache. By default, uses environment variable
 * `GRADLE_ENTERPRISE_API_MAX_CACHE_SIZE` or ~1 GB.
 */
var maxCacheSize = System.getenv("GRADLE_ENTERPRISE_API_MAX_CACHE_SIZE")?.toLong()
    ?: 1_000_000_000L

/**
 * HTTP cache location. By default, uses environment variable `GRADLE_ENTERPRISE_API_CACHE_DIR`
 * or the system temporary folder (`java.io.tmpdir` / gradle-enterprise-api-kotlin-cache).
 */
var cacheDir = System.getenv("GRADLE_ENTERPRISE_API_CACHE_DIR")?.let(::File)
    ?: File(System.getProperty("java.io.tmpdir"), "gradle-enterprise-api-kotlin-cache")

/**
 * Enables debug logging from the library. All logging is output to the program's standard streams.
 * By default, uses environment variable `GRADLE_ENTERPRISE_API_DEBUG_LOGGING` or `false`.
 */
var debugLoggingEnabled = System.getenv("GRADLE_ENTERPRISE_API_DEBUG_LOGGING").toBoolean()
