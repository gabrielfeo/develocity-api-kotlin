package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.Options
import com.gabrielfeo.gradle.enterprise.api.internal.infrastructure.Serializer
import com.gabrielfeo.gradle.enterprise.api.options
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

internal val retrofit: Retrofit by lazy {
    buildRetrofit(
        options,
        okHttpClient,
        Serializer.moshi,
    )
}

internal fun buildRetrofit(
    options: Options,
    client: OkHttpClient,
    moshi: Moshi,
) = with(Retrofit.Builder()) {
    val url = options.gradleEnterpriseInstance.url()
    check("/api" !in url) { "Instance URL must be the plain instance URL, without /api" }
    baseUrl(url)
    addConverterFactory(ScalarsConverterFactory.create())
    addConverterFactory(MoshiConverterFactory.create(moshi))
    client(client)
    build()
}
