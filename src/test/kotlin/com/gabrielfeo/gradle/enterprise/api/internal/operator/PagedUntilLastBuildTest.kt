package com.gabrielfeo.gradle.enterprise.api.internal.operator

import com.gabrielfeo.gradle.enterprise.api.GradleEnterpriseApiStub
import com.gabrielfeo.gradle.enterprise.api.model.Build
import com.gabrielfeo.gradle.enterprise.api.model.FakeBuild
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PagedUntilLastBuildTest {

    private val api = object : GradleEnterpriseApiStub {
        var getBuildsCallCount = 0
        override suspend fun getBuilds(
            since: Long?,
            sinceBuild: String?,
            fromInstant: Long?,
            fromBuild: String?,
            reverse: Boolean?,
            maxBuilds: Int?,
            maxWaitSecs: Int?,
        ): List<Build> {
            getBuildsCallCount++
            check((since ?: sinceBuild ?: fromInstant ?: reverse ?: maxWaitSecs) == null)
            checkNotNull(fromBuild)
            checkNotNull(maxBuilds)
            val first = builds.indexOfFirst { it.id == fromBuild } + 1
            return builds
                .slice(first..builds.lastIndex)
                .take(maxBuilds)
        }
    }

    private val builds = listOf(
        FakeBuild(id = "a", availableAt = 1),
        FakeBuild(id = "b", availableAt = 2),
        FakeBuild(id = "c", availableAt = 3),
        FakeBuild(id = "d", availableAt = 4),
        FakeBuild(id = "e", availableAt = 5),
    )

    @Test
    fun `Pages and stops once API sends less than maxBuilds`() = runTest {
        val channel = Channel<Build>(Channel.RENDEZVOUS)
        flowOf(builds[0], builds[1])
            .pagedUntilLastBuild(api, maxBuilds = 4)
            .onEach { channel.send(it) }
            .launchIn(this)
        assertEquals(builds[0], channel.receive())
        // should wait until original Flow is collected before requesting more builds
        assertEquals(0, api.getBuildsCallCount)
        assertEquals(builds[1], channel.receive())
        // should request more now, after last of original Flow was collected
        assertEquals(builds[2], channel.receive())
        assertEquals(builds[3], channel.receive())
        assertEquals(builds[4], channel.receive())
        assertEquals(1, api.getBuildsCallCount)
        // should presume no more builds and complete, since maxBuilds=2 and only received 1
        assertTrue(channel.tryReceive().isFailure)
        channel.close()
    }

    @Test
    fun `Pages and stops once API sends empty list`() = runTest {
        val channel = Channel<Build>(Channel.RENDEZVOUS)
        flowOf(builds[0])
            .pagedUntilLastBuild(api, maxBuilds = 2)
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
}