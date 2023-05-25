package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.Config
import com.gabrielfeo.gradle.enterprise.api.GradleEnterpriseApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class GradleEnterpriseApiIntegrationTest {

    @Test
    fun canFetchBuildsWithDefaultInstance() = runTest {
        env = RealEnv
        keychain = RealKeychain(RealSystemProperties)
        val builds = GradleEnterpriseApi.buildsApi.getBuilds(since = 0, maxBuilds = 1)
        assertEquals(1, builds.size)
        GradleEnterpriseApi.shutdown()
    }

    @Test
    fun canBuildNewInstanceWithPureCodeConfiguration() = runTest {
        env = FakeEnv()
        keychain = FakeKeychain()
        assertDoesNotThrow {
            val config = Config(
                apiUrl = "https://google.com/api/",
                apiToken = { "" },
            )
            GradleEnterpriseApi.newInstance(config)
        }
    }
}