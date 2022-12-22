package com.gabrielfeo.gradle.enterprise.api

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.logging.Level.INFO
import java.util.logging.Logger
import kotlin.time.Duration

@Suppress("ObjectPropertyName")
var _debugLoggingEnabled = System.getenv("GRADLE_ENTERPRISE_API_DEBUG_LOGGING").toBoolean()

@OptIn(DelicateCoroutinesApi::class)
internal fun OkHttpClient.logRequestCountEvery(period: Duration) {
    GlobalScope.launch {
        val logger = Logger.getGlobal()
        while (!dispatcher.executorService.isShutdown) {
            val count = dispatcher.runningCallsCount()
            logger.log(INFO, "$count requests running")
            delay(period)
        }
    }
}
