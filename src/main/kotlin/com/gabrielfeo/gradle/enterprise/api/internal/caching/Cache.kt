package com.gabrielfeo.gradle.enterprise.api.internal.caching

import com.gabrielfeo.gradle.enterprise.api.cacheDir
import com.gabrielfeo.gradle.enterprise.api.debugLoggingEnabled
import com.gabrielfeo.gradle.enterprise.api.maxCacheSize
import okhttp3.Cache
import java.util.logging.Level.INFO
import java.util.logging.Logger

internal val cache: Cache = run {
    if (debugLoggingEnabled) {
        Logger.getGlobal().log(INFO, "HTTP cache dir with max size $maxCacheSize: $cacheDir")
    }
    Cache(cacheDir, maxSize = maxCacheSize)
}
