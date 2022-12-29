package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import retrofit2.await

private const val API_MAX_BUILDS = 1000

/**
 * Gets builds on demand from the API, in as many requests as necessary. It allows
 * for queries of any size, as opposed to [GradleEnterpriseApi.getBuilds] which is limited by the
 * API itself to 1000.
 *
 * - Will request from the API until results end or an error occurs.
 * - Use [Sequence.take] and similar functions to stop collecting early.
 * - A subset of `getBuilds` params are supported
 */
fun GradleEnterpriseApi.getBuildsFlow(
    since: Long = 0,
    sinceBuild: String? = null,
    fromInstant: Long? = null,
    fromBuild: String? = null,
): Flow<Build> = flow {
    var lastBuildId: String? = null
    while (true) {
        val call = when (lastBuildId) {
            null -> getBuilds(
                since = since, sinceBuild = sinceBuild,
                fromInstant = fromInstant, fromBuild = fromBuild,
                maxBuilds = API_MAX_BUILDS,
            )
            else -> getBuilds(fromBuild = lastBuildId, maxBuilds = API_MAX_BUILDS)
        }
        val builds = call.await()
        emitAll(builds.asFlow())
        when {
            builds.isEmpty() || builds.size < API_MAX_BUILDS -> break
            else -> lastBuildId = builds.last().id
        }
    }
}

/**
 * Joins builds with their [GradleAttributes], which comes from a different endpoint
 * ([GradleEnterpriseApi.getGradleAttributes]).
 *
 * Don't expect client-side filtering to be efficient. Does as many concurrent calls
 * as it can, requesting attributes in an eager coroutine, in [scope].
 */
fun Flow<Build>.withGradleAttributes(
    scope: CoroutineScope = GlobalScope,
): Flow<Pair<Build, GradleAttributes>> =
    map { build ->
        build to scope.async {
            api.getGradleAttributes(build.id).await()
        }
    }.buffer(Int.MAX_VALUE).map { (build, attrs) ->
        build to attrs.await()
    }

/**
 * Gets [GradleAttributes] of all builds from a given date. Queries [GradleEnterpriseApi.getBuilds]
 * first, since it's the only endpoint providing a timeline of builds, then maps each to
 * [GradleEnterpriseApi.getGradleAttributes].
 *
 * Don't expect client-side filtering to be efficient. Does as many concurrent calls
 * as it can, requesting attributes in an eager coroutine, in [scope]. For other params,
 * see [getBuildsFlow] and [GradleEnterpriseApi.getBuilds].
 */
fun GradleEnterpriseApi.getGradleAttributesFlow(
    since: Long = 0,
    sinceBuild: String? = null,
    fromInstant: Long? = null,
    fromBuild: String? = null,
    scope: CoroutineScope = GlobalScope,
): Flow<GradleAttributes> =
    getBuildsFlow(
        since = since,
        sinceBuild = sinceBuild,
        fromInstant = fromInstant,
        fromBuild = fromBuild
    ).withGradleAttributes(scope).map { (_, attrs) ->
        attrs
    }
