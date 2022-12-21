@file:Suppress("RemoveExplicitTypeArguments")

package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.infrastructure.Serializer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create

var baseUrl: String? = null
var accessToken: String? = null

var maxConcurrentRequests = 50

val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .addInterceptor(HttpBearerAuth("bearer", accessToken ?: requireToken()))
        .build()
        .apply {
            dispatcher.maxRequests = maxConcurrentRequests
            dispatcher.maxRequestsPerHost = maxConcurrentRequests
        }
}

val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(baseUrl ?: requireBaseUrl())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(Serializer.moshi))
        .client(okHttpClient)
        .build()
}

val api: GradleEnterpriseApi by lazy {
    retrofit.create<GradleEnterpriseApi>()
}

fun shutdown() = okHttpClient.dispatcher.executorService.shutdown()
