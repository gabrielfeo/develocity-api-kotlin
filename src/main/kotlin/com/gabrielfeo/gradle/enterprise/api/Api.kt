package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.*
import retrofit2.create

val api: GradleEnterpriseApi by lazy {
    retrofit.create(GradleEnterpriseApi::class.java)
}

fun shutdown() {
    okHttpClient.dispatcher.executorService.shutdownNow()
}

var maxCacheSize = 500_000_000L
val cacheablePaths: MutableList<Regex> = mutableListOf(
    """.*/api/builds/[\d\w]+/(?:gradle|maven)-attributes""".toRegex(),
)

var baseUrl: () -> String = { requireBaseUrl() }
var accessToken: () -> String = { requireToken() }

var maxConcurrentRequests = 30
var debugLoggingEnabled = System.getenv("GRADLE_ENTERPRISE_API_DEBUG_LOGGING").toBoolean()
