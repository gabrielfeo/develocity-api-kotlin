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
    fun `mapToGradleAttributes maps builds in order`() = runTest {
        val attrs = api.builds.asFlow().mapToGradleAttributes(api, scope = this).toList()
        assertEquals(5, callCount)
        assertEquals(5, attrs.size)
        attrs.indices.forEach { i ->
            assertEquals(attrs[i].id, api.builds[i].id)
        }
    }

    @Test
    fun `mapToGradleAttributes(bufferSize=1), then GradleAttributes fetched eagerly`() =
        testWithNeverEndingCollector(bufferSize = 1, expectedRequests = 3)

    @Test
    fun `mapToGradleAttributes(bufferSize=0), then GradleAttributes fetched lazily`() =
        testWithNeverEndingCollector(bufferSize = 0, expectedRequests = 1)

    @Test
    fun `mapToGradleAttributes(bufferSize=-1), then GradleAttributes fetched lazily`() =
        testWithNeverEndingCollector(bufferSize = -1, expectedRequests = 1)

    private fun testWithNeverEndingCollector(
        bufferSize: Int,
        expectedRequests: Int,
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
                .filter { it == expectedRequests }
                .first()
        } ?: fail("Expected $expectedRequests calls, got $callCount")
    }
}
