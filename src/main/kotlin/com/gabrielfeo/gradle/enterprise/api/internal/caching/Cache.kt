package com.gabrielfeo.gradle.enterprise.api.internal.caching

import com.gabrielfeo.gradle.enterprise.api.baseUrl
import com.gabrielfeo.gradle.enterprise.api.maxCacheSize
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File

internal val cache: Cache = run {
    val host = baseUrl().toHttpUrl().host
    val tempDir = System.getProperty("java.io.tmpdir")
    Cache(
        directory = File(tempDir, "gradle-enterprise-api-cache-$host"),
        maxSize = maxCacheSize,
    )
}
