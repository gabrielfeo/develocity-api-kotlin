package com.gabrielfeo.gradle.enterprise.api.internal.caching

import com.gabrielfeo.gradle.enterprise.api.Options
import okhttp3.Cache
import java.util.logging.Level.INFO
import java.util.logging.Logger

internal fun buildCache(): Cache {
    if (Options.debugLoggingEnabled) {
        val logger = Logger.getGlobal()
        logger.log(INFO, "HTTP cache dir: ${Options.cacheDir} (max ${Options.maxCacheSize}B)")
    }
    return Cache(Options.cacheDir, maxSize = Options.maxCacheSize)
}
