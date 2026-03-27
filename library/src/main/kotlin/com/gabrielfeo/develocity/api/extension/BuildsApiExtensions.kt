@file:Suppress("unused")

package com.gabrielfeo.develocity.api.extension

import com.gabrielfeo.develocity.api.BuildsApi
import com.gabrielfeo.develocity.api.internal.API_MAX_BUILDS
import com.gabrielfeo.develocity.api.model.*
import kotlinx.coroutines.flow.*

/**
 * Gets builds on demand from the API, in as many requests as necessary. It allows
 * for queries of any size, as opposed to [BuildsApi.getBuilds] which is limited by the
 * API itself to 1000.
 *
 * - Will request from the API until results end, collection stops or an error occurs.
 * - Parameters same as [BuildsApi.getBuilds].
 * - Using [query] is highly recommended for server-side filtering (equivalent to Develocity advanced
 * query).
 * - `maxBuilds` is the only unsupported parameter, because this Flow will instead fetch
 * continously. Use [Flow.take] to stop collecting at a specific count.
 */
fun BuildsApi.getBuildsFlow(
    since: Long? = null,
    sinceBuild: String? = null,
    fromInstant: Long? = null,
    fromBuild: String? = null,
    query: String? = null,
    reverse: Boolean? = null,
    maxWaitSecs: Int? = null,
    buildsPerPage: Int = API_MAX_BUILDS,
    models: List<BuildModelName>? = null,
    allModels: Boolean? = false,
): Flow<Build> {
    return flow {
        var builds = getBuilds(
            since = since,
            sinceBuild = sinceBuild,
            fromInstant = fromInstant,
            fromBuild = fromBuild,
            query = query,
            reverse = reverse,
            maxWaitSecs = maxWaitSecs,
            maxBuilds = buildsPerPage,
            models = models,
            allModels = allModels,
        )
        emitAll(builds.asFlow())
        while (builds.isNotEmpty()) {
            builds = getBuilds(
                fromBuild = builds.last().id,
                query = query,
                reverse = reverse,
                maxWaitSecs = maxWaitSecs,
                maxBuilds = buildsPerPage,
                models = models,
                allModels = allModels,
            )
            emitAll(builds.asFlow())
        }
    }
}
