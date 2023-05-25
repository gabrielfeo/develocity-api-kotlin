package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.GradleEnterpriseApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GradleEnterpriseApiIntegrationTest {

    @Test
    fun canFetchBuildsWithDefaultInstance() = runTest {
        val builds = GradleEnterpriseApi.buildsApi.getBuilds(since = 0, maxBuilds = 1)
        assertEquals(1, builds.size)
        GradleEnterpriseApi.shutdown()
    }

//    @Test
//    fun canBuildNewInstanceWithCodeConfiguration() = runTest {
//        val builds = GradleEnterpriseApi.buildsApi.getBuilds(since = 0, maxBuilds = 1)
//        assertEquals(1, builds.size)
//        GradleEnterpriseApi.shutdown()
//    }
}