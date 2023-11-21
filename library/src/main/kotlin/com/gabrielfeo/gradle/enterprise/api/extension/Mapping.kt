package com.gabrielfeo.gradle.enterprise.api.extension

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Maps [Build]s ([BuildsApi.getBuilds]) to their [GradleAttributes]
 * ([BuildsApi.getGradleAttributes]).
 *
 * Instead of filtering builds downstream based on `GradleAttributes` (e.g. using [Flow.filter]),
 * prefer filtering server-side before mapping (see [BuildsApi.getBuilds]).
 *
 * ### Buffering
 *
 * If [bufferSize] > 0, will request attributes lazily as builds are collected, else will request
 * eagerly and buffer up to [bufferSize] calls.
 *
 * ### Concurrency
 *
 * Attributes are requested concurrently in coroutines started in [scope]. The number of
 * concurrent requests underneath is still limited by [Config.maxConcurrentRequests].
 *
 * @param scope CoroutineScope in which to create coroutines. If bufferSize < 1, no coroutines
 * are started. Defaults to [GlobalScope].
 * @param bufferSize Buffer capacity (see [Flow.buffer])
 */
@OptIn(DelicateCoroutinesApi::class)
fun Flow<Build>.mapToGradleAttributes(
    api: BuildsApi,
    scope: CoroutineScope = GlobalScope,
    bufferSize: Int = Int.MAX_VALUE,
): Flow<GradleAttributes> {
    if (bufferSize < 1) {
        return map { build ->
            api.getGradleAttributes(build.id)
        }
    }
    return map { build ->
        scope.async {
            api.getGradleAttributes(build.id)
        }
    }.buffer(bufferSize).map {
        it.await()
    }
}