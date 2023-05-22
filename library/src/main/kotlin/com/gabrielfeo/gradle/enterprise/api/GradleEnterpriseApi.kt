package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.buildOkHttpClient
import com.gabrielfeo.gradle.enterprise.api.internal.buildRetrofit
import com.gabrielfeo.gradle.enterprise.api.internal.infrastructure.Serializer
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
 * For simple use cases, you may use the companion instance ([DefaultInstance]) directly, as if
 * calling static methods:
 *
 * ```kotlin
 * GradleEnterpriseApi.buildsApi.getBuilds(...)
 * ```
 *
 * However, if you need to change [options] at runtime or own the instance's lifecycle (e.g.
 * with an IoC container like Dagger), create a new instance:
 *
 * ```kotlin
 * val options = Options(clientBuilder = myOwnOkHttpClient.newBuilder())
 * val api = GradleEnterpriseApi.newInstance(options)
 * api.buildsApi.getBuilds(...)
 * ```
 */
interface GradleEnterpriseApi {

    val buildsApi: BuildsApi
    val buildCacheApi: BuildCacheApi
    val metaApi: MetaApi
    val testDistributionApi: TestDistributionApi

    /**
     * Library configuration options.
     */
    val options: Options

    /**
     * Release resources allowing the program to finish before the internal client's idle timeout.
     */
    fun shutdown()

    /**
     * The default, companion instance of the Gradle Enterprise API client. See
     * [GradleEnterpriseApi].
     */
    companion object DefaultInstance : GradleEnterpriseApi by RealGradleEnterpriseApi() {

        /**
         * Create a new instance of `GradleEnterpriseApi` with new options.
         */
        fun newInstance(options: Options): GradleEnterpriseApi {
            return RealGradleEnterpriseApi(options)
        }
    }

}

private class RealGradleEnterpriseApi(
    override val options: Options = Options(),
) : GradleEnterpriseApi {

    private val okHttpClient by lazy {
        buildOkHttpClient(options = options)
    }

    private val retrofit: Retrofit by lazy {
        buildRetrofit(
            options,
            okHttpClient,
            Serializer.moshi,
        )
    }

    override val buildsApi: BuildsApi by lazy(retrofit::create)
    override val buildCacheApi: BuildCacheApi by lazy(retrofit::create)
    override val metaApi: MetaApi by lazy(retrofit::create)
    override val testDistributionApi: TestDistributionApi by lazy(retrofit::create)

    override fun shutdown() {
        okHttpClient.run {
            dispatcher.executorService.shutdown()
            connectionPool.evictAll()
            cache?.close()
        }
    }
}
