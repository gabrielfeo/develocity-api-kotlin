package com.gabrielfeo.gradle.enterprise.api

/**
 * A Sequence of builds that requests [pageSize] builds per request. Will request until
 * builds end or an error occurs.
 *
 * Callers can use [Sequence.take] and similar functions to stop collecting.
 */
fun GradleEnterpriseApi.buildsSequence(
    since: Long = 0,
    pageSize: Int = 50,
) = sequence {
    var lastBuildId: String? = null
    while(true) {
        val response = getBuilds(since = since, maxBuilds = pageSize, fromBuild = lastBuildId)
            .execute()
        val builds = response.body() ?: error("ERROR ${response.code()}: ${response.errorBody()}")
        if (builds.isNotEmpty()) {
            yieldAll(builds)
            lastBuildId = builds.last().id
        } else {
            break
        }
    }
}
