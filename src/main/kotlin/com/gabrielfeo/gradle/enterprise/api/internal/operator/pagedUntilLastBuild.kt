package com.gabrielfeo.gradle.enterprise.api.internal.operator

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.internal.API_MAX_BUILDS
import com.gabrielfeo.gradle.enterprise.api.model.*
import kotlinx.coroutines.flow.*
import retrofit2.await

// TODO Test
/**
 * Emits all available builds starting from the upstream Flow builds until the last build available.
 * Makes paged requests to the API using `fromBuild`, [maxPerRequest] at a time.
 */
internal fun Flow<Build>.pagedUntilLastBuild(
    maxPerRequest: Int,
): Flow<Build> {
    val firstBuilds = this
    return flow {
        var lastBuildId = ""
        firstBuilds.collect {
            lastBuildId = it.id
            emit(it)
        }
        if (lastBuildId.isEmpty()) {
            return@flow
        } else while (true) {
            val builds = api.getBuilds(fromBuild = lastBuildId, maxBuilds = maxPerRequest)
            emitAll(builds.asFlow())
            when {
                builds.isEmpty() || builds.size < API_MAX_BUILDS -> break
                else -> lastBuildId = builds.last().id
            }
        }
    }
}
