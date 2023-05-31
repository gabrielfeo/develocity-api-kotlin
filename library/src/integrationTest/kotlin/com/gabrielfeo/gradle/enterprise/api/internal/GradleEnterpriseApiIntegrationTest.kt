package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.Config
import com.gabrielfeo.gradle.enterprise.api.GradleEnterpriseApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.Test

class GradleEnterpriseApiIntegrationTest {

    @Test
    fun canFetchBuildsWithDefaultConfig() = runTest {
        env = RealEnv
        keychain = RealKeychain(RealSystemProperties)
        val api = GradleEnterpriseApi.newInstance()
        val builds = api.buildsApi.getBuilds(since = 0, maxBuilds = 1)
        assertEquals(1, builds.size)
        api.shutdown()
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