package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.GradleEnterpriseApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GradleEnterpriseApiIntegrationTest {

    @Test
    fun canFetchBuilds() = runTest {
        val builds = GradleEnterpriseApi.buildsApi.getBuilds(since = 0, maxBuilds = 1)
        assertEquals(1, builds.size)
        GradleEnterpriseApi.shutdown()
    }
}