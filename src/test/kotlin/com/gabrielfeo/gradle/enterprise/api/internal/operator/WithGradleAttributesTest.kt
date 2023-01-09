package com.gabrielfeo.gradle.enterprise.api.internal.operator

import com.gabrielfeo.gradle.enterprise.api.GradleEnterpriseApiStub
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

    private val api = object : GradleEnterpriseApiStub {
        val callCount = MutableStateFlow(0)
        override suspend fun getGradleAttributes(
            id: String,
            availabilityWaitTimeoutSecs: Int?,
        ): GradleAttributes {
            callCount.value++
            val attrs = readFromJsonResource<GradleAttributes>("gradle-attributes-response.json")
            return attrs.copy(id = id)
        }
    }

    private val builds = flowOf(
        FakeBuild(id = "a", availableAt = 1),
        FakeBuild(id = "b", availableAt = 2),
        FakeBuild(id = "c", availableAt = 3),
        FakeBuild(id = "d", availableAt = 4),
        FakeBuild(id = "e", availableAt = 5),
    )

    @Test
    fun `Pairs each build with its GradleAttributes`() = runTest {
        val buildsToAttrs = builds.withGradleAttributes(scope = this, api).toList()
        assertEquals(5, api.callCount.value)
        assertEquals(5, buildsToAttrs.size)
        buildsToAttrs.forEach { (build, attrs) ->
            assertEquals(build.id, attrs.id)
        }
    }

    @Test
    fun `Fetches GradleAttributes for all builds eagerly`() = runTest {
        backgroundScope.launch {
            builds.withGradleAttributes(scope = this, api).collect {
                // Make the first collect never complete, simulating a slow collector
                Job().join()
            }
        }
        // Expect 5 eager calls despite slow collector
        withTimeoutOrNull(2.seconds) {
            api.callCount.take(5).collect()
        }
        assertEquals(5, api.callCount.value)
    }
}
