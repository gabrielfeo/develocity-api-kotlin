package com.gabrielfeo.develocity.api.extension

import com.gabrielfeo.develocity.api.*
import com.gabrielfeo.develocity.api.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Joins builds with their [GradleAttributes], which comes from a different endpoint
 * ([BuildsApi.getGradleAttributes]).
 *
 * Don't expect client-side filtering to be efficient. Does as many concurrent calls
 * as it can, requesting attributes in an eager coroutine, in [scope].
 */
internal fun Flow<Build>.withGradleAttributes(
    scope: CoroutineScope,
    api: BuildsApi,
): Flow<Pair<Build, GradleAttributes>> =
    map { build ->
        build to scope.async {
            api.getGradleAttributes(build.id)
        }
    }.buffer(Int.MAX_VALUE).map { (build, attrs) ->
        build to attrs.await()
    }
