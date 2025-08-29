package com.gabrielfeo.develocity.api.internal

import okhttp3.OkHttpClient

interface OkHttpClientBuilderFactory {

    companion object {
        var default: OkHttpClientBuilderFactory = FreshOkHttpClientBuilderFactory()
    }

    fun create(): OkHttpClient.Builder
}

/**
 * Creates a new [OkHttpClient.Builder] instance for every call to [create].
 */
class FreshOkHttpClientBuilderFactory : OkHttpClientBuilderFactory {
    override fun create() = OkHttpClient.Builder()
}

/**
 * Re-uses the same [OkHttpClient.Builder] instance for every call to [create], allowing for
 * internal resources such as connection pools and threads to be shared between clients.
 */
class SharedOkHttpClientBuilderFactory : OkHttpClientBuilderFactory {
    private val sharedClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }
    override fun create() = sharedClient.newBuilder()
}
