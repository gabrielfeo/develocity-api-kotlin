package com.gabrielfeo.gradle.enterprise.api.internal.operator

import com.gabrielfeo.gradle.enterprise.api.FakeBuildsApi
import com.gabrielfeo.gradle.enterprise.api.extension.mapToGradleAttributes
import com.gabrielfeo.gradle.enterprise.api.model.FakeBuild
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail
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

    private val callCount
        get() = api.getGradleAttributesCallCount.value

    @Test
    fun `Maps each build to its GradleAttributes in order`() = runTest {
        val attrs = api.builds.asFlow().mapToGradleAttributes(api, scope = this).toList()
        assertEquals(5, callCount)
        assertEquals(5, attrs.size)
        attrs.indices.forEach { i ->
            assertEquals(attrs[i].id, api.builds[i].id)
        }
    }

    @Test
    fun `When bufferSize is 1, fetches GradleAttributes for builds eagerly`() =
        testWithSlowCollector(bufferSize = 1, expectedCallsBeforeCollect = 3)

    @Test
    fun `When bufferSize is 0, fetches GradleAttributes for builds lazily`() =
        testWithSlowCollector(bufferSize = 0, expectedCallsBeforeCollect = 1)

    @Test
    fun `When bufferSize is -1, behavior same as 0`() =
        testWithSlowCollector(bufferSize = -1, expectedCallsBeforeCollect = 1)

    private fun testWithSlowCollector(
        bufferSize: Int,
        expectedCallsBeforeCollect: Int,
    ) = runTest {
        backgroundScope.launch {
            api.builds.asFlow().mapToGradleAttributes(api, scope = this, bufferSize)
                .collect {
                    // Make the first collect never complete, simulating a slow collector
                    Job().join()
                }
        }
        withTimeoutOrNull(2.seconds) {
            api.getGradleAttributesCallCount
                .filter { it == expectedCallsBeforeCollect }
                .first()
        } ?: fail("Expected $expectedCallsBeforeCollect calls, got $callCount")
    }
}
