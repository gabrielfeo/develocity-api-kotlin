package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.buildOkHttpClient
import com.gabrielfeo.gradle.enterprise.api.internal.buildRetrofit
import com.gabrielfeo.gradle.enterprise.api.internal.infrastructure.Serializer
import retrofit2.Retrofit
import retrofit2.create

interface GradleEnterpriseApi {

    val buildsApi: BuildsApi
    val buildCacheApi: BuildCacheApi
    val metaApi: MetaApi
    val testDistributionApi: TestDistributionApi

    val options: Options
    fun withOptions(options: Options): GradleEnterpriseApi
    fun shutdown()

    companion object : GradleEnterpriseApi by DefaultGradleEnterpriseApi()

}

private class DefaultGradleEnterpriseApi(
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

    override fun withOptions(options: Options) = DefaultGradleEnterpriseApi(options)


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
