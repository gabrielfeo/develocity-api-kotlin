package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.RealLoggerFactory
import com.gabrielfeo.gradle.enterprise.api.internal.buildOkHttpClient
import com.gabrielfeo.gradle.enterprise.api.internal.buildRetrofit
import com.gabrielfeo.gradle.enterprise.api.internal.infrastructure.Serializer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create

/**
 * Gradle Enterprise API client. API endpoints are grouped exactly as in the
 * [Gradle Enterprise API Manual](https://docs.gradle.com/enterprise/api-manual/#reference_documentation):
 *
 * - [buildsApi]
 * - [buildCacheApi]
 * - [metaApi]
 * - [testDistributionApi]
 *
 * Create an instance with [newInstance]:
 *
 * ```kotlin
 * val api = GradleEnterpriseApi.newInstance()
 * api.buildsApi.getBuilds(...)
 * ```
 *
 * You may pass a default [Config], e.g. for sharing [OkHttpClient] resources:
 *
 * ```kotlin
 * val options = Options(clientBuilder = myOwnOkHttpClient.newBuilder())
 * val api = GradleEnterpriseApi.newInstance(options)
 * api.buildsApi.getBuilds(...)
 * ```
 */
interface GradleEnterpriseApi {

    val authApi: AuthApi
    val buildsApi: BuildsApi
    val buildCacheApi: BuildCacheApi
    val projectsApi: ProjectsApi
    val testsApi: TestsApi
    val metaApi: MetaApi
    val testDistributionApi: TestDistributionApi

    /**
     * Library configuration options.
     */
    val config: Config

    /**
     * Release resources allowing the program to finish before the internal client's idle timeout.
     */
    fun shutdown()

    companion object {

        /**
         * Create a new instance of `GradleEnterpriseApi` with a custom `Config`.
         */
        fun newInstance(config: Config = Config()): GradleEnterpriseApi {
            return RealGradleEnterpriseApi(config)
        }
    }

}

internal class RealGradleEnterpriseApi(
    override val config: Config,
) : GradleEnterpriseApi {

    private val okHttpClient by lazy {
        buildOkHttpClient(config = config, RealLoggerFactory(config))
    }

    private val retrofit: Retrofit by lazy {
        buildRetrofit(
            config,
            okHttpClient,
            Serializer.moshi,
        )
    }

    override val authApi: AuthApi by lazy { retrofit.create() }
    override val buildsApi: BuildsApi by lazy { retrofit.create() }
    override val buildCacheApi: BuildCacheApi by lazy { retrofit.create() }
    override val projectsApi: ProjectsApi by lazy { retrofit.create() }
    override val testsApi: TestsApi by lazy { retrofit.create() }
    override val metaApi: MetaApi by lazy { retrofit.create() }
    override val testDistributionApi: TestDistributionApi by lazy { retrofit.create() }

    override fun shutdown() {
        okHttpClient.run {
            dispatcher.executorService.shutdown()
            connectionPool.evictAll()
            cache?.close()
        }
    }
}
