@file:Suppress("unused")

package com.gabrielfeo.develocity.api.extension

import com.gabrielfeo.develocity.api.Config
import com.gabrielfeo.develocity.api.BuildsApi
import com.gabrielfeo.develocity.api.internal.API_MAX_BUILDS
import com.gabrielfeo.develocity.api.model.*
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
@Deprecated(
    "Use `getBuildsFlow(models = listOf(BuildModelName.gradleAttributes))` instead. " +
        "This function will be removed in the next release.",
    replaceWith = ReplaceWith(
        "getBuildsFlow(since, sinceBuild, fromInstant, fromBuild, query, reverse," +
            "maxWaitSecs, models = listOf(BuildModelName.gradleAttributes))",
        imports = [
            "com.gabrielfeo.develocity.api.extension.getBuildsFlow",
            "com.gabrielfeo.develocity.api.model.BuildModelName",
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
    models: List<BuildModelName>? = null,
): Flow<GradleAttributes> =
    getBuildsFlow(
        since = since,
        sinceBuild = sinceBuild,
        fromInstant = fromInstant,
        fromBuild = fromBuild,
        query = query,
        reverse = reverse,
        maxWaitSecs = maxWaitSecs,
        models = models,
    ).withGradleAttributes(scope, api = this).map { (_, attrs) ->
        attrs
    }
