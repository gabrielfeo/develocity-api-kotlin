package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.gradleEnterpriseApi
import com.gabrielfeo.gradle.enterprise.api.shutdown
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GradleEnterpriseIntegrationTest {

    @Test
    fun canFetchBuilds() = runTest {
        val builds = gradleEnterpriseApi.getBuilds(since = 0, maxBuilds = 1)
        assertEquals(1, builds.size)
        shutdown()
    }
}