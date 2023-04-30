package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.model.*

/**
 * Scaffold for a fake `GradleEnterpriseApi` implementation with default methods throwing a
 * [NotImplementedError]. Extend this interface and override methods to fake behavior as needed.
 */
interface FakeGradleEnterpriseApiScaffold : GradleEnterpriseApi {

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

    override suspend fun createTestDistributionAgentPool(
        testDistributionAgentPoolConfiguration: TestDistributionAgentPoolConfiguration,
    ): TestDistributionAgentPoolConfigurationWithId {
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

    override suspend fun getBuilds(
        since: Long?,
        sinceBuild: String?,
        fromInstant: Long?,
        fromBuild: String?,
        reverse: Boolean?,
        maxBuilds: Int?,
        maxWaitSecs: Int?,
    ): List<Build> {
        TODO("Not yet implemented")
    }

    override suspend fun getGradleAttributes(
        id: String,
        availabilityWaitTimeoutSecs: Int?,
    ): GradleAttributes {
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

    override suspend fun getTestDistributionAgentPool(
        poolId: String,
    ): TestDistributionAgentPoolConfigurationWithId {
        TODO("Not yet implemented")
    }

    override suspend fun getTestDistributionAgentPoolStatus(
        poolId: String,
    ): TestDistributionAgentPoolStatus {
        TODO("Not yet implemented")
    }

    override suspend fun getTestDistributionApiKey(
        keyPrefix: String,
    ): TestDistributionApiKeyPrefix {
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