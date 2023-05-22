package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.*
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.util.logging.Logger

/**
 * Library configuration options. Should not be changed after accessing the [gradleEnterpriseApi]
 * object for the first time.
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
)

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
