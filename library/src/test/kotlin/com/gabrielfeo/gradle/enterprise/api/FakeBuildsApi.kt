package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.http.Query

class FakeBuildsApi(
    val builds: List<Build>,
) : FakeBuildsApiScaffold {

    val getBuildsCallCount = MutableStateFlow(0)
    val getGradleAttributesCallCount = MutableStateFlow(0)

    override suspend fun getBuilds(
        since: Long?,
        sinceBuild: String?,
        fromInstant: Long?,
        fromBuild: String?,
        reverse: Boolean?,
        maxBuilds: Int?,
        maxWaitSecs: Int?,
        query: String?,
        models: List<BuildModelName>?,
        allModels: Boolean?,
    ): List<Build> {
        getBuildsCallCount.value++
        check((reverse ?: maxWaitSecs ?: query ?: models) == null) { "Not supported" }
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
        getGradleAttributesCallCount.value++
        val attrs = readFromJsonResource<GradleAttributes>("gradle-attributes-response.json")
        return attrs.copy(id = id)
    }
}
