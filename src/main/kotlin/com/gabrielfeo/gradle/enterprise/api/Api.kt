@file:Suppress("RemoveExplicitTypeArguments")

package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.infrastructure.Serializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import kotlin.time.Duration.Companion.seconds

var baseUrl: () -> String = { requireBaseUrl() }
var accessToken: () -> String = { requireToken() }
var maxConcurrentRequests = 30

val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .addInterceptor(HttpBearerAuth("bearer", accessToken()))
        .addInterceptor(HttpLoggingInterceptor())
        .build()
        .apply {
            dispatcher.maxRequests = maxConcurrentRequests
            dispatcher.maxRequestsPerHost = maxConcurrentRequests
            if (_debugLoggingEnabled) {
                logRequestCountEvery(2.seconds)
            }
        }
}

val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(baseUrl())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(Serializer.moshi))
        .client(okHttpClient)
        .build()
}

val api: GradleEnterpriseApi by lazy {
    retrofit.create<GradleEnterpriseApi>()
}

fun shutdown() {
    okHttpClient.dispatcher.executorService.shutdownNow()
}
