package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.model.*
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGradleEnterpriseApi(
    val builds: List<Build>,
) : FakeGradleEnterpriseApiScaffold {

    val getBuildsCallCount = MutableStateFlow(0)
    val getGradleAtrributesCallCount = MutableStateFlow(0)

    override suspend fun getBuilds(
        since: Long?,
        sinceBuild: String?,
        fromInstant: Long?,
        fromBuild: String?,
        reverse: Boolean?,
        maxBuilds: Int?,
        maxWaitSecs: Int?,
    ): List<Build> {
        getBuildsCallCount.value++
        check((reverse ?: maxWaitSecs) == null)
        if ((fromBuild ?: sinceBuild) != null) {
            check((since ?: fromInstant) == null) { "Invalid request" }
        }
        if (since != null) {
            check(since == 0L) { "Filtering by date is not implemented" }
            check((fromBuild ?: sinceBuild) == null) { "Invalid request" }
        }
        if (fromInstant != null) {
            check(fromInstant == 0L) { "Filtering by date is not implemented" }
            check((fromBuild ?: sinceBuild) == null) { "Invalid request" }
        }
        checkNotNull(maxBuilds)
        val first = builds.indexOfFirst { it.id == (fromBuild ?: sinceBuild) } + 1
        return builds
            .slice(first..builds.lastIndex)
            .take(maxBuilds)
    }

    override suspend fun getGradleAttributes(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): GradleAttributes {
        getGradleAtrributesCallCount.value++
        val attrs = readFromJsonResource<GradleAttributes>("gradle-attributes-response.json")
        return attrs.copy(id = id)
    }
}