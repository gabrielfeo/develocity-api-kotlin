package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.model.*
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGradleEnterpriseApi(
    val builds: List<Build>,
) : GradleEnterpriseApi {

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

    override suspend fun createOrUpdateBuildCacheNode(
        name: String,
        nodeConfiguration: NodeConfiguration,
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun createOrUpdateTestDistributionAgentPool(
        poolId: String,
        testDistributionAgentPoolConfiguration: TestDistributionAgentPoolConfiguration,
    ): TestDistributionAgentPoolConfigurationWithId {
        TODO("Not yet implemented")
    }

    override suspend fun createTestDistributionAgentPool(testDistributionAgentPoolConfiguration: TestDistributionAgentPoolConfiguration): TestDistributionAgentPoolConfigurationWithId {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTestDistributionAgentPool(poolId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun generateTestDistributionApiKey(): TestDistributionApiKey {
        TODO("Not yet implemented")
    }

    override suspend fun getBuild(id: String, availabilityWaitTimeoutSecs: Int?): Build {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildCacheNode(name: String): NodeConfiguration {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleBuildCachePerformance(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): GradleBuildCachePerformance {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleProjects(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): List<GradleProject> {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenAttributes(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): MavenAttributes {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenBuildCachePerformance(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): MavenBuildCachePerformance {
        TODO("Not yet implemented")
    }

    override suspend fun getMavenModules(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): List<MavenModule> {
        TODO("Not yet implemented")
    }

    override suspend fun getTestDistributionAgentPool(poolId: String): TestDistributionAgentPoolConfigurationWithId {
        TODO("Not yet implemented")
    }

    override suspend fun getTestDistributionAgentPoolStatus(poolId: String): TestDistributionAgentPoolStatus {
        TODO("Not yet implemented")
    }

    override suspend fun getTestDistributionApiKey(keyPrefix: String): TestDistributionApiKeyPrefix {
        TODO("Not yet implemented")
    }

    override suspend fun getVersion(): GradleEnterpriseVersion {
        TODO("Not yet implemented")
    }

    override suspend fun initiatePurgeOfBuildCacheNode(name: String) {
        TODO("Not yet implemented")
    }

    override suspend fun insertTestDistributionApiKey(
        keyPrefix: String,
        testDistributionApiKey: TestDistributionApiKey,
    ): TestDistributionApiKeyPrefix {
        TODO("Not yet implemented")
    }

    override suspend fun listTestDistributionAgentPools(): TestDistributionAgentPoolPage {
        TODO("Not yet implemented")
    }

    override suspend fun listTestDistributionApiKeys(): TestDistributionApiKeyPrefixPage {
        TODO("Not yet implemented")
    }

    override suspend fun regenerateSecretOfBuildCacheNode(name: String): KeySecretPair {
        TODO("Not yet implemented")
    }

    override suspend fun revokeTestDistributionApiKey(keyPrefix: String) {
        TODO("Not yet implemented")
    }
}