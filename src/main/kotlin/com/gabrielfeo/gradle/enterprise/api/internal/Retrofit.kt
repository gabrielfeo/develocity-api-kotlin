package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.Options
import com.gabrielfeo.gradle.enterprise.api.internal.infrastructure.Serializer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

internal val retrofit: Retrofit by lazy {
    val url = Options.GradleEnterpriseInstance.url()
    check("/api" !in url) { "Instance URL must be the plain instance URL, without /api" }
    Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(Serializer.moshi))
        .client(okHttpClient)
        .build()
}
