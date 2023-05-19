package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.buildOkHttpClient
import com.gabrielfeo.gradle.enterprise.api.internal.buildRetrofit
import com.gabrielfeo.gradle.enterprise.api.internal.infrastructure.Serializer
import retrofit2.Retrofit
import retrofit2.create

interface GradleEnterprise {

    val buildsApi: BuildsApi
    val buildCacheApi: BuildCacheApi
    val metaApi: MetaApi
    val testDistributionApi: TestDistributionApi

    val options: Options
    fun withOptions(options: Options): GradleEnterprise
    fun shutdown()

    companion object : GradleEnterprise by DefaultGradleEnterprise()

}

private class DefaultGradleEnterprise(
    override val options: Options = Options(),
) : GradleEnterprise {

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

    override fun withOptions(options: Options) = DefaultGradleEnterprise(options)


    /**
     * Release resources allowing the program to finish before the internal client's idle timeout.
     */
    override fun shutdown() {
        okHttpClient.run {
            dispatcher.executorService.shutdown()
            connectionPool.evictAll()
            cache?.close()
        }
    }
}
