package com.gabrielfeo.develocity.api.internal

import com.gabrielfeo.develocity.api.Config
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
    val base = config.develocityUrl
    // Ensure trailing slash for URL joining
    val baseWithSlash = if (base.endsWith("/")) base else "$base/"
    val apiUrl = baseWithSlash + "api/"
    runCatching { java.net.URI(apiUrl) }.onFailure { error ->
        throw IllegalArgumentException("A valid API URL could not be constructed from develocityUrl: $base", error)
    }
    baseUrl(apiUrl)
    addConverterFactory(ScalarsConverterFactory.create())
    addConverterFactory(MoshiConverterFactory.create(moshi))
    client(client)
    build()
}
