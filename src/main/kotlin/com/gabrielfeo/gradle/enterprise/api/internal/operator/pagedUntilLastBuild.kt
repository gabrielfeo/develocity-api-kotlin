package com.gabrielfeo.gradle.enterprise.api.internal.operator

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.internal.API_MAX_BUILDS
import com.gabrielfeo.gradle.enterprise.api.model.*
import kotlinx.coroutines.flow.*

/**
 * Emits all available builds starting from the upstream Flow builds until the last build available.
 * Makes paged requests to the API using `fromBuild`, [maxBuilds] at a time.
 */
internal fun Flow<Build>.pagedUntilLastBuild(
    api: GradleEnterpriseApi,
    maxBuilds: Int,
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
            val builds = api.getBuilds(fromBuild = lastBuildId, maxBuilds = maxBuilds)
            emitAll(builds.asFlow())
            when {
                builds.isEmpty() || builds.size < maxBuilds -> break
                else -> lastBuildId = builds.last().id
            }
        }
    }
}
