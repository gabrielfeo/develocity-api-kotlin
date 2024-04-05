package com.gabrielfeo.develocity.api.extension

import com.gabrielfeo.develocity.api.FakeBuildsApi
import com.gabrielfeo.develocity.api.model.FakeBuild
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class MappingTest {

    private val api = FakeBuildsApi(
        builds = listOf(
            FakeBuild(id = "a", availableAt = 1),
            FakeBuild(id = "b", availableAt = 2),
            FakeBuild(id = "c", availableAt = 3),
            FakeBuild(id = "d", availableAt = 4),
            FakeBuild(id = "e", availableAt = 5),
        )
    )

    @Test
    fun `withGradleAttributes pairs each build with its GradleAttributes`() = runTest {
        val buildsToAttrs = api.builds.asFlow().withGradleAttributes(scope = this, api).toList()
        assertEquals(5, api.getGradleAttributesCallCount.value)
        assertEquals(5, buildsToAttrs.size)
        buildsToAttrs.forEach { (build, attrs) ->
            assertEquals(build.id, attrs.id)
        }
    }

    @Test
    fun `withGradleAttributes fetches GradleAttributes for all builds eagerly`() = runTest {
        backgroundScope.launch {
            api.builds.asFlow().withGradleAttributes(scope = this, api).collect {
                // Make the first collect never complete, simulating a slow collector
                Job().join()
            }
        }
        // Expect 5 eager calls despite slow collector
        withTimeoutOrNull(2.seconds) {
            api.getGradleAttributesCallCount.take(5).collect()
        }
        assertEquals(5, api.getGradleAttributesCallCount.value)
    }
}
