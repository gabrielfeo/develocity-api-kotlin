package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.baseUrl
import com.gabrielfeo.gradle.enterprise.api.internal.infrastructure.Serializer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

internal val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(baseUrl())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(Serializer.moshi))
        .client(okHttpClient)
        .build()
}
