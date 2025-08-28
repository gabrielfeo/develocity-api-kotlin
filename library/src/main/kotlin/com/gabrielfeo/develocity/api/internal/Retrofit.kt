package com.gabrielfeo.develocity.api.internal

import com.gabrielfeo.develocity.api.*
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

internal fun buildRetrofit(
    config: Config,
    client: OkHttpClient,
    moshi: Moshi,
) = with(Retrofit.Builder()) {
    baseUrl(config.server.resolve("/").toString())
    addConverterFactory(ScalarsConverterFactory.create())
    addConverterFactory(MoshiConverterFactory.create(moshi))
    client(client)
    build()
}
