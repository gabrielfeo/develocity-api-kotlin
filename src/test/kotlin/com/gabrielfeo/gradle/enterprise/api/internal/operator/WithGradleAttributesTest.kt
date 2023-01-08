package com.gabrielfeo.gradle.enterprise.api.internal.operator

import com.gabrielfeo.gradle.enterprise.api.GradleEnterpriseApiStub
import com.gabrielfeo.gradle.enterprise.api.model.Build
import com.gabrielfeo.gradle.enterprise.api.model.FakeBuild
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WithGradleAttributesTest {

    private val api = object : GradleEnterpriseApiStub {

    }

    private val builds = ArrayDeque<Build>(6).apply {
        add(FakeBuild(id = "a", availableAt = 1))
        add(FakeBuild(id = "b", availableAt = 2))
        add(FakeBuild(id = "c", availableAt = 3))
        add(FakeBuild(id = "d", availableAt = 4))
        add(FakeBuild(id = "e", availableAt = 5))
        add(FakeBuild(id = "f", availableAt = 6))
    }

    @Test
    fun `2 builds from upstream, 1 more from paging`() = runTest {
        builds.asFlow().withGradleAttributes(scope = this, api)
    }
}