package com.gabrielfeo.develocity.api.extension

import com.gabrielfeo.develocity.api.FakeBuildsApi
import com.gabrielfeo.develocity.api.model.Build
import com.gabrielfeo.develocity.api.model.FakeBuild
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildsApiExtensionsTest {

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
    fun `getBuildsFlow with low maxBuilds pages until empty response`() = runTest {
        val channel = Channel<Build>(Channel.RENDEZVOUS)
        api.getBuildsFlow(buildsPerPage = 2)
            .onEach { channel.send(it) }
            .launchIn(this)
        // Collect page 1, expecting 1 request so far
        assertEquals(api.builds[0], channel.receive())
        assertEquals(1, api.getBuildsCallCount.value)
        assertEquals(api.builds[1], channel.receive())
        // Page 1 exhausted. Collect page 2 expecting new request
        assertEquals(api.builds[2], channel.receive())
        assertEquals(2, api.getBuildsCallCount.value)
        assertEquals(api.builds[3], channel.receive())
        // Page 2 exhausted. Collect page 3 expecting 2 new requests (last is empty)
        assertEquals(api.builds[4], channel.receive())
        assertTrue(channel.tryReceive().isFailure)
        assertEquals(4, api.getBuildsCallCount.value)
        channel.close()
    }

    @Test
    fun `getBuildsFlow with high maxBuilds pages until empty response`() = runTest {
        val channel = Channel<Build>(Channel.RENDEZVOUS)
        api.getBuildsFlow(buildsPerPage = api.builds.size)
            .onEach { channel.send(it) }
            .launchIn(this)
        // Collect page 1 (with all builds), expecting 1 request so far
        assertEquals(api.builds[0], channel.receive())
        assertEquals(1, api.getBuildsCallCount.value)
        assertEquals(api.builds[1], channel.receive())
        assertEquals(api.builds[2], channel.receive())
        assertEquals(api.builds[3], channel.receive())
        assertEquals(api.builds[4], channel.receive())
        // Page 1 exhausted. Expect no more builds, despite new request
        assertTrue(channel.tryReceive().isFailure)
        assertEquals(2, api.getBuildsCallCount.value)
        channel.close()
    }
}
