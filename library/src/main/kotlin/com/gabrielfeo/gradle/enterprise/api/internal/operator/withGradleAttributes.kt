package com.gabrielfeo.gradle.enterprise.api.internal.operator

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.await

/**
 * Joins builds with their [GradleAttributes], which comes from a different endpoint
 * ([GradleEnterpriseApi.getGradleAttributes]).
 *
 * Don't expect client-side filtering to be efficient. Does as many concurrent calls
 * as it can, requesting attributes in an eager coroutine, in [scope].
 */
internal fun Flow<Build>.withGradleAttributes(
    scope: CoroutineScope,
    api: GradleEnterpriseApi,
): Flow<Pair<Build, GradleAttributes>> =
    map { build ->
        build to scope.async {
            api.getGradleAttributes(build.id)
        }
    }.buffer(Int.MAX_VALUE).map { (build, attrs) ->
        build to attrs.await()
    }
