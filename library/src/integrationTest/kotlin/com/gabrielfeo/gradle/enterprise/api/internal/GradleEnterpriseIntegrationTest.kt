package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.GradleEnterprise
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GradleEnterpriseIntegrationTest {

    @Test
    fun canFetchBuilds() = runTest {
        val builds = GradleEnterprise.api.getBuilds(since = 0, maxBuilds = 1)
        assertEquals(1, builds.size)
        GradleEnterprise.shutdown()
    }
}