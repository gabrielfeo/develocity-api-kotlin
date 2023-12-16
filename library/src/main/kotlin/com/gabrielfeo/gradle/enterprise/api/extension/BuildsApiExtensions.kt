@file:Suppress("unused")

package com.gabrielfeo.gradle.enterprise.api.extension

import com.gabrielfeo.gradle.enterprise.api.Config
import com.gabrielfeo.gradle.enterprise.api.BuildsApi
import com.gabrielfeo.gradle.enterprise.api.internal.API_MAX_BUILDS
import com.gabrielfeo.gradle.enterprise.api.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*

/**
 * Gets builds on demand from the API, in as many requests as necessary. It allows
 * for queries of any size, as opposed to [BuildsApi.getBuilds] which is limited by the
 * API itself to 1000.
 *
 * - Will request from the API until results end, collection stops or an error occurs.
 * - Parameters same as [BuildsApi.getBuilds].
 * - Using [query] is highly recommended for server-side filtering (equivalent to GE advanced
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
        )
        emitAll(builds.asFlow())
        while (builds.isNotEmpty()) {
            builds = getBuilds(
                fromBuild = builds.last().id,
                query = query,
                reverse = reverse,
                maxWaitSecs = maxWaitSecs,
                maxBuilds = buildsPerPage,
            )
            emitAll(builds.asFlow())
        }
    }
}

/**
 * Gets [GradleAttributes] of all builds from a given date. Queries [BuildsApi.getBuilds] first,
 * the endpoint providing a timeline of builds, then maps each to [BuildsApi.getGradleAttributes].
 *
 * Instead of filtering builds downstream based on `GradleAttributes` (e.g. using [Flow.filter]),
 * prefer filtering server-side using a `query` (see [BuildsApi.getBuilds]).
 *
 * ### Buffering
 *
 * Will request eagerly and buffer up to [Int.MAX_VALUE] calls.
 *
 * ### Concurrency
 *
 * Attributes are requested concurrently in coroutines started in [scope]. The number of
 * concurrent requests underneath is still limited by [Config.maxConcurrentRequests].
 *
 * @param scope CoroutineScope in which to create coroutines. Defaults to [GlobalScope].
 */
@OptIn(DelicateCoroutinesApi::class)
fun BuildsApi.getGradleAttributesFlow(
    since: Long = 0,
    sinceBuild: String? = null,
    fromInstant: Long? = null,
    fromBuild: String? = null,
    query: String? = null,
    reverse: Boolean? = null,
    maxWaitSecs: Int? = null,
    scope: CoroutineScope = GlobalScope,
): Flow<GradleAttributes> =
    getBuildsFlow(
        since = since,
        sinceBuild = sinceBuild,
        fromInstant = fromInstant,
        fromBuild = fromBuild,
        query = query,
        reverse = reverse,
        maxWaitSecs = maxWaitSecs,
    ).withGradleAttributes(scope, api = this).map { (_, attrs) ->
        attrs
    }
