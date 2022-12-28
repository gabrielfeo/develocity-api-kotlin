package com.gabrielfeo.gradle.enterprise.api.internal.caching

import com.gabrielfeo.gradle.enterprise.api.baseUrl
import com.gabrielfeo.gradle.enterprise.api.debugLoggingEnabled
import com.gabrielfeo.gradle.enterprise.api.maxCacheSize
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import java.util.logging.Level.INFO
import java.util.logging.Logger

internal val cache: Cache = run {
    val host = baseUrl().toHttpUrl().host
    val tempDir = System.getProperty("java.io.tmpdir")
    val dir = File(tempDir, "gradle-enterprise-api-cache-$host")
    if (debugLoggingEnabled) {
        Logger.getGlobal().log(INFO, "HTTP cache dir: $dir")
    }
    Cache(dir, maxSize = maxCacheSize)
}
