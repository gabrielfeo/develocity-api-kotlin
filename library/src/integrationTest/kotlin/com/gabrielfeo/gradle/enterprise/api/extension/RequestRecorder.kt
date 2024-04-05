package com.gabrielfeo.develocity.api.extension

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*

class RequestRecorder {

    val requests = LinkedList<Request>()

    fun clientBuilder() = OkHttpClient.Builder()
        .addNetworkInterceptor {
            requests += it.request()
            it.proceed(it.request())
        }
}
