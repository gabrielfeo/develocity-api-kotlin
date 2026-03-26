package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.model.*
import retrofit2.http.Query

/**
 * Scaffold for a fake `DevelocityApi` implementation with default methods throwing a
 * [NotImplementedError]. Extend this interface and override methods to fake behavior as needed.
 */
interface FakeBuildsApiScaffold : BuildsApi {

    override suspend fun getBuild(
        id: String,
        models: List<BuildModelName>?,
        allModels: Boolean?,
        availabilityWaitTimeoutSecs: Int?,
        skipUnavailableModels: Boolean?,
    ): Build {
        TODO("Not yet implemented")
    }

    override suspend fun getBazelAttributes(
        id: String,
        availabilityWaitTimeoutSecs: Int?
    ): BazelAttributes {
        TODO("Not yet implemented")
    }

    override suspend fun getBazelCriticalPath(
        id: String,
        availabilityWaitTimeoutSecs: Int?
    ): BazelCriticalPath {
        TODO("Not yet implemented")
    }

    override suspend fun getBuilds(
        fromInstant: Long?,
        fromBuild: String?,
        reverse: Boolean?,
        maxBuilds: Int?,
        maxWaitSecs: Int?,
        query: String?,
        models: List<BuildModelName>?,
        allModels: Boolean?,
        since: Long?,
        sinceBuild: String?,
        skipUnavailableModels: Boolean?,
    ): List<Build> {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleBuildProfileOverview(
        id: String,
        availabilityWaitTimeoutSecs: Int?
    ): GradleBuildProfileOverview {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleConfigurationCache(
        id: String,
        availabilityWaitTimeoutSecs: Int?
    ): GradleConfigurationCache {
        TODO("Not yet implemented")
    }

    override suspend fun getGradlePlugins(
        id: String,
        availabilityWaitTimeoutSecs: Int?
    ): GradlePlugins {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleResourceUsage(
        id: String,
        availabilityWaitTimeoutSecs: Int?
    ): GradleResourceUsage {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenBuildProfileOverview(
        id: String,
        availabilityWaitTimeoutSecs: Int?
    ): MavenBuildProfileOverview {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenPlugins(
        id: String,
        availabilityWaitTimeoutSecs: Int?
    ): MavenPlugins {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenResourceUsage(
        id: String,
        availabilityWaitTimeoutSecs: Int?
    ): MavenResourceUsage {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleArtifactTransformExecutions(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): GradleArtifactTransformExecutions {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleAttributes(id: String, availabilityWaitTimeoutSecs: Int?): GradleAttributes {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleBuildCachePerformance(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): GradleBuildCachePerformance {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleDeprecations(id: String, availabilityWaitTimeoutSecs: Int?): GradleDeprecations {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleNetworkActivity(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): GradleNetworkActivity {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleProjects(id: String, availabilityWaitTimeoutSecs: Int?): List<GradleProject> {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenAttributes(id: String, availabilityWaitTimeoutSecs: Int?): MavenAttributes {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenBuildCachePerformance(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): MavenBuildCachePerformance {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenDependencyResolution(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): MavenDependencyResolution {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenModules(id: String, availabilityWaitTimeoutSecs: Int?): List<MavenModule> {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleTestPerformance(
        id: String,
        availabilityWaitTimeoutSecs: Int?
    ): GradleTestPerformance {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenTestPerformance(id: String, availabilityWaitTimeoutSecs: Int?): MavenTestPerformance {
        TODO("Not yet implemented")
    }

    override suspend fun getNpmAttributes(id: String, availabilityWaitTimeoutSecs: Int?): NpmAttributes {
        TODO("Not yet implemented")
    }

    override suspend fun getPythonAttributes(id: String, availabilityWaitTimeoutSecs: Int?): PythonAttributes {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleDependencies(id: String, availabilityWaitTimeoutSecs: Int?): GradleDependencies {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenDependencies(id: String, availabilityWaitTimeoutSecs: Int?): MavenDependencies {
        TODO("Not yet implemented")
    }

    override suspend fun getSbtAttributes(id: String, availabilityWaitTimeoutSecs: Int?): SbtAttributes {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenExtensions(id: String, availabilityWaitTimeoutSecs: Int?): MavenExtensions {
        TODO("Not yet implemented")
    }

    override suspend fun getNpmDependencies(id: String, availabilityWaitTimeoutSecs: Int?): NpmDependencies {
        TODO("Not yet implemented")
    }

    override suspend fun getNpmNetworkActivity(id: String, availabilityWaitTimeoutSecs: Int?): NpmNetworkActivity {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleDependencyCaching(id: String, availabilityWaitTimeoutSecs: Int?): GradleDependencyCaching {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenDependencyCaching(id: String, availabilityWaitTimeoutSecs: Int?): MavenDependencyCaching {
        TODO("Not yet implemented")
    }
}

/**
 * Scaffold for a fake `FailuresApi` implementation with default methods throwing a
 * [NotImplementedError]. Extend this interface and override methods to fake behavior as needed.
 */
interface FakeFailuresApiScaffold : FailuresApi {

    override suspend fun getBuildFailures(id: String, buildFailuresQuery: BuildFailuresQuery?): BuildFailures {
        TODO("Not yet implemented")
    }

    override suspend fun getFailureGroups(
        failureTypes: List<FailureType>?,
        query: String,
        maxFailureGroups: Int?,
        maxBuildIdsPerGroup: Int?,
    ): FailureGroupsResponse {
        TODO("Not yet implemented")
    }
}
