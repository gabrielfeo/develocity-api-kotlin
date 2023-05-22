package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.extension.getBuildsFlow
import com.gabrielfeo.gradle.enterprise.api.extension.getGradleAttributesFlow
import com.gabrielfeo.gradle.enterprise.api.model.FakeBuild
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class GradleEnterpriseApiExtensionsTest {

    private val api = FakeBuildsApi(
        builds = listOf(
            FakeBuild(id = "a", availableAt = 1),
            FakeBuild(id = "b", availableAt = 2),
            FakeBuild(id = "c", availableAt = 3),
            FakeBuild(id = "d", availableAt = 4),
            FakeBuild(id = "e", availableAt = 5),
            FakeBuild(id = "f", availableAt = 6),
        )
    )

    @Test
    // TODO In this case, the 2nd request could be avoided
    fun `Given single page, getBuildsFlow calls API twice and emits all builds`() = runTest {
        val builds = api.getBuildsFlow(since = 0, buildsPerPage = 1000).toList()
        check(api.builds.size < 1000)
        // On 2nd time gets empty and completes
        assertEquals(2, api.getBuildsCallCount.value)
        assertEquals(6, builds.size)
    }

    @Test
    fun `Given 3 pages, getBuildsFlow calls API 4 times and emits all builds`() = runTest {
        check(api.builds.size == 6)
        val builds = api.getBuildsFlow(since = 0, buildsPerPage = 2).toList()
        // On 4th time gets empty list and completes
        assertEquals(4, api.getBuildsCallCount.value)
        assertEquals(6, builds.size)
    }

    @Test
    fun `getGradleAttributesFlow calls getGradleAttributes once per build, eagerly`() = runTest {
        backgroundScope.launch {
            api.getGradleAttributesFlow(scope = this).collect {
                // Make the first collect never complete, simulating a slow collector
                Job().join()
            }
        }
        // Expect one eager call per build despite slow collector
        withTimeoutOrNull(2.seconds) {
            api.getGradleAttributesCallCount.take(api.builds.size).collect()
        }
        assertEquals(api.builds.size, api.getGradleAttributesCallCount.value)
    }
}
