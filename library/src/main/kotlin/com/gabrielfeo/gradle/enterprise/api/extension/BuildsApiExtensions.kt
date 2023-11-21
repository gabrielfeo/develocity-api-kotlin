@file:Suppress("unused")

package com.gabrielfeo.gradle.enterprise.api.extension

import com.gabrielfeo.gradle.enterprise.api.Config
import com.gabrielfeo.gradle.enterprise.api.BuildsApi
import com.gabrielfeo.gradle.enterprise.api.internal.API_MAX_BUILDS
import com.gabrielfeo.gradle.enterprise.api.internal.operator.pagedUntilLastBuild
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
    since: Long = 0,
    sinceBuild: String? = null,
    fromInstant: Long? = null,
    fromBuild: String? = null,
    query: String? = null,
    reverse: Boolean? = null,
    maxWaitSecs: Int? = null,
    buildsPerPage: Int = API_MAX_BUILDS,
): Flow<Build> {
    val api = this
    return flow {
        val firstBuilds = getBuilds(
            since = since,
            sinceBuild = sinceBuild,
            fromInstant = fromInstant,
            fromBuild = fromBuild,
            query = query,
            reverse = reverse,
            maxWaitSecs = maxWaitSecs,
            maxBuilds = buildsPerPage,
        )
        val pagedBuilds = firstBuilds.asFlow().pagedUntilLastBuild(api, buildsPerPage)
        emitAll(pagedBuilds)
    }
}

/**
 * Gets [GradleAttributes] of all builds from a given date. Queries [BuildsApi.getBuilds] first,
 * the endpoint providing a timeline of builds, then maps each to [BuildsApi.getGradleAttributes].
 *
 * Instead of filtering builds downstream based on `GradleAttributes` (e.g. using [Flow.filter]),
 * prefer filtering server-side before mapping (see [BuildsApi.getBuilds]).
 *
 * ### Buffering
 *
 * Will request eagerly and buffer up to [Int.MAX_VALUE] calls. To set buffer size, use
 * [BuildsApi.getBuildsFlow] + [mapToGradleAttributes] instead.
 *
 * ### Concurrency
 *
 * Attributes are requested concurrently in coroutines started in [scope]. The number of
 * concurrent requests underneath is still limited by [Config.maxConcurrentRequests].
 *
 * @param scope CoroutineScope in which to create coroutines. If bufferSize < 1, no coroutines
 * are started. Defaults to [GlobalScope].
 */
@Deprecated(
    "Use mapToGradleAttributes instead. This function will be removed in the next release.",
    replaceWith = ReplaceWith(
        "getBuildsFlow(since, sinceBuild, fromInstant, fromBuild, query, reverse, maxWaitSecs)" +
            ".mapToGradleAttributes(api, scope)",
        imports = [
            "com.gabrielfeo.gradle.enterprise.api.extension.getBuildsFlow",
            "com.gabrielfeo.gradle.enterprise.api.extension.mapToGradleAttributes",
        ]
    ),
)
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
    ).mapToGradleAttributes(api = this, scope = scope)
