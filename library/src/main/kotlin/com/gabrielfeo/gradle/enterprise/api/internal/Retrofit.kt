package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.Options
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

internal fun buildRetrofit(
    options: Options,
    client: OkHttpClient,
    moshi: Moshi,
) = with(Retrofit.Builder()) {
    val url = options.apiUrl
    check("/api/" in url) { "A valid API URL must end in /api/" }
    val instanceUrl = url.substringBefore("api/")
    baseUrl(instanceUrl)
    addConverterFactory(ScalarsConverterFactory.create())
    addConverterFactory(MoshiConverterFactory.create(moshi))
    client(client)
    build()
}
