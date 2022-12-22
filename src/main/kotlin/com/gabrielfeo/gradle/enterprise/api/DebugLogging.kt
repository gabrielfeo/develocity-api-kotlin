package com.gabrielfeo.gradle.enterprise.api

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.logging.Level.INFO
import java.util.logging.Logger
import kotlin.time.Duration

@Suppress("ObjectPropertyName")
var _debugLoggingEnabled = System.getenv("GRADLE_ENTERPRISE_API_DEBUG_LOGGING").toBoolean()

internal class CacheHitLoggingInterceptor(
    private val logger: Logger = Logger.getGlobal(),
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!_debugLoggingEnabled) {
            return chain.proceed(chain.request())
        }
        val url = chain.request().url
        val response = chain.proceed(chain.request())
        val wasHit = with(response) { cacheResponse != null && networkResponse == null }
        val hitOrMiss = if (wasHit) "hit" else "miss"
        logger.log(INFO, "Cache $hitOrMiss: $url")
        return response
    }
}

@OptIn(DelicateCoroutinesApi::class)
internal fun startRequestCountLogging(
    client: OkHttpClient,
    period: Duration,
    logger: Logger = Logger.getGlobal(),
) {
    if (!_debugLoggingEnabled) {
        return
    }
    GlobalScope.launch {
        while (!client.dispatcher.executorService.isShutdown) {
            val count = client.dispatcher.runningCallsCount()
            logger.log(INFO, "$count requests running")
            delay(period)
        }
    }
}
