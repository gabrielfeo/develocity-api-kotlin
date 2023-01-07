package com.gabrielfeo.gradle.enterprise.api.internal.operator

import app.cash.turbine.test
import com.gabrielfeo.gradle.enterprise.api.FakeGradleEnterpriseApi
import com.gabrielfeo.gradle.enterprise.api.internal.API_MAX_BUILDS
import com.gabrielfeo.gradle.enterprise.api.model.Build
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PagedUntilLastBuildTest {

    private val builds = ArrayDeque<Build>(6).apply {
        add(newBuild(id = "a", availableAt = 1))
        add(newBuild(id = "b", availableAt = 2))
        add(newBuild(id = "c", availableAt = 3))
        add(newBuild(id = "d", availableAt = 4))
        add(newBuild(id = "e", availableAt = 5))
        add(newBuild(id = "f", availableAt = 6))
    }

    @Test
    fun `2 builds from upstream, 1 more from paging`() = runTest {
        val api = FakeGradleEnterpriseApi(
            getBuildsDelegate = { args ->
                when (val count = args.callCount) {
                    1 -> {
                        // should request builds after the last collected build
                        assertEquals(builds[1].id, args.fromBuild)
                        assertEquals(2, args.maxBuilds)
                        // sending last available build
                        listOf(builds[2])
                    }
                    else -> throw AssertionError("Expected 1 API call, got $count")
                }
            }
        )
        val channel = Channel<Build>(Channel.RENDEZVOUS)
        flowOf(builds[0], builds[1])
            .pagedUntilLastBuild(api, maxBuilds = 2, maxBuildsAllowed = API_MAX_BUILDS)
            .onEach { channel.send(it) }
            .launchIn(this)
        assertEquals(builds[0], channel.receive())
        // should wait until original Flow is collected before requesting more builds
        assertEquals(0, api.getBuildsCallCount)
        assertEquals(builds[1], channel.receive())
        // should request more now, after last of original Flow was collected
        assertEquals(builds[2], channel.receive())
        assertEquals(1, api.getBuildsCallCount)
        // should presume no more builds and complete, since maxBuilds=2 and only received 1
        assertTrue(channel.tryReceive().isFailure)
        channel.close()
    }

    @Test
    fun `1 build from upstream, 4 more from paging`() = runTest {
        val api = FakeGradleEnterpriseApi(
            getBuildsDelegate = { args ->
                when (val count = args.callCount) {
                    1 -> {
                        // should request builds after the last collected build
                        assertEquals(builds[0].id, args.fromBuild)
                        assertEquals(2, args.maxBuilds)
                        // sending last available build
                        listOf(builds[1], builds[2])
                    }
                    2 -> {
                        // should request builds after the last collected build
                        assertEquals(builds[2].id, args.fromBuild)
                        assertEquals(2, args.maxBuilds)
                        // sending last available build
                        listOf(builds[3], builds[4])
                    }
                    3 -> {
                        // should request builds after the last collected build
                        assertEquals(builds[4].id, args.fromBuild)
                        assertEquals(2, args.maxBuilds)
                        // sending last available build
                        emptyList()
                    }
                    else -> throw AssertionError("Expected 3 API calls, got $count")
                }
            }
        )
        val channel = Channel<Build>(Channel.RENDEZVOUS)
        flowOf(builds[0])
            .pagedUntilLastBuild(api, maxBuilds = 2, maxBuildsAllowed = API_MAX_BUILDS)
            .onEach { channel.send(it) }
            .launchIn(this)
        // should wait until original Flow is collected before requesting more builds
        assertEquals(0, api.getBuildsCallCount)
        assertEquals(builds[0], channel.receive())
        // should request more now, after last of original Flow was collected
        assertEquals(builds[1], channel.receive())
        // should wait until current page builds are collected
        assertEquals(1, api.getBuildsCallCount)
        assertEquals(builds[2], channel.receive())
        assertEquals(builds[3], channel.receive())
        // should wait until current page builds are collected
        assertEquals(2, api.getBuildsCallCount)
        // should request more now
        assertEquals(builds[4], channel.receive())
        // should presume no more builds and complete, since received no builds
        assertTrue(channel.tryReceive().isFailure)
        channel.close()
    }

    private fun newBuild(id: String, availableAt: Long) = Build(
        id = id,
        availableAt = availableAt,
        buildToolType = "",
        buildToolVersion = "",
        buildAgentVersion = "",
    )
}