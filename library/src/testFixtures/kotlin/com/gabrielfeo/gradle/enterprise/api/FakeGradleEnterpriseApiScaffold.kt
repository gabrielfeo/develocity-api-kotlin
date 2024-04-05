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
    ): Build {
        TODO("Not yet implemented")
    }

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
}
