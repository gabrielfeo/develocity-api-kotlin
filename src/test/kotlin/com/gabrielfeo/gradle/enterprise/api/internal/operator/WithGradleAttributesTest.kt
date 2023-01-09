package com.gabrielfeo.gradle.enterprise.api.internal.operator

import com.gabrielfeo.gradle.enterprise.api.FakeGradleEnterpriseApi
import com.gabrielfeo.gradle.enterprise.api.model.FakeBuild
import com.gabrielfeo.gradle.enterprise.api.model.GradleAttributes
import com.gabrielfeo.gradle.enterprise.api.readFromJsonResource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class WithGradleAttributesTest {

    private val api = FakeGradleEnterpriseApi(
        builds = listOf(
            FakeBuild(id = "a", availableAt = 1),
            FakeBuild(id = "b", availableAt = 2),
            FakeBuild(id = "c", availableAt = 3),
            FakeBuild(id = "d", availableAt = 4),
            FakeBuild(id = "e", availableAt = 5),
        )
    )

    @Test
    fun `Pairs each build with its GradleAttributes`() = runTest {
        val buildsToAttrs = api.builds.asFlow().withGradleAttributes(scope = this, api).toList()
        assertEquals(5, api.getGradleAtrributesCallCount.value)
        assertEquals(5, buildsToAttrs.size)
        buildsToAttrs.forEach { (build, attrs) ->
            assertEquals(build.id, attrs.id)
        }
    }

    @Test
    fun `Fetches GradleAttributes for all builds eagerly`() = runTest {
        backgroundScope.launch {
            api.builds.asFlow().withGradleAttributes(scope = this, api).collect {
                // Make the first collect never complete, simulating a slow collector
                Job().join()
            }
        }
        // Expect 5 eager calls despite slow collector
        withTimeoutOrNull(2.seconds) {
            api.getGradleAtrributesCallCount.take(5).collect()
        }
        assertEquals(5, api.getGradleAtrributesCallCount.value)
    }
}
