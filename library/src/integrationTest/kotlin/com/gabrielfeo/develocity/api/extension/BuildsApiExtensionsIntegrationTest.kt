package com.gabrielfeo.develocity.api.extension

import com.gabrielfeo.develocity.api.*
import com.gabrielfeo.develocity.api.internal.*
import com.gabrielfeo.develocity.api.model.BuildModelName
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import okhttp3.Request
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.time.Duration.Companion.minutes

class BuildsApiExtensionsIntegrationTest {

    private val recorder = RequestRecorder()
    private lateinit var api: DevelocityApi
    private lateinit var mockWebServer: okhttp3.mockwebserver.MockWebServer

    @BeforeTest
    fun setup() {
        mockWebServer = okhttp3.mockwebserver.MockWebServer()
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setBody("[]"))
        mockWebServer.start()
        env = FakeEnv()
        api = buildApi(recorder)
    }

    @AfterTest
    fun teardown() {
        api.shutdown()
        mockWebServer.shutdown()
    }

    @Test
    fun getBuildsFlowPreservesParamsAcrossRequests() = runTest(timeout = 6.minutes) {
        api.buildsApi.getBuildsFlow(
            since = 0,
            query = "user:*",
            models = listOf(BuildModelName.gradleAttributes),
            allModels = true,
            reverse = true,
            buildsPerPage = 2,
        ).take(4).collect()
        recorder.requests.forEach {
            assertUrlParam(it, "query", "user:*")
            assertUrlParam(it, "models", "gradle-attributes")
            assertUrlParam(it, "allModels", "true")
            assertUrlParam(it, "reverse", "true")
        }
    }

    @Test
    fun getBuildsFlowReplacesSinceForFromBuildAfterFirstRequest() = runTest {
        api.buildsApi.getBuildsFlow(since = 1, buildsPerPage = 2).take(10).collect()
        assertReplacedForFromBuildAfterFirstRequest(param = "since" to "1")
    }

    @Test
    fun getBuildsFlowReplacesFromInstantForFromBuildAfterFirstRequest() = runTest {
        api.buildsApi.getBuildsFlow(fromInstant = 1, buildsPerPage = 2).take(10).collect()
        assertReplacedForFromBuildAfterFirstRequest(param = "fromInstant" to "1")
    }

    private fun assertReplacedForFromBuildAfterFirstRequest(param: Pair<String, String>) {
        with(recorder.requests) {
            val (key, value) = param
            first().let {
                assertUrlParam(it, key, value)
                assertUrlParam(it, "fromBuild", null)
            }
            (this - first()).forEach {
                assertUrlParam(it, key, null)
                assertUrlParamNotNull(it, "fromBuild")
            }
        }
    }

    private fun buildApi(recorder: RequestRecorder) =
        DevelocityApi.newInstance(
            config = Config(
                server = mockWebServer.url("/").toUri(),
                accessKey = { "${mockWebServer.url("/").host}=foo" },
                clientBuilder = recorder.clientBuilder(),
                cacheConfig = Config.CacheConfig(cacheEnabled = false),
            )
        )

    private fun assertUrlParam(request: Request, key: String, expected: String?) {
        val actual = request.url.queryParameter(key)
        assertEquals(expected, actual, "Expected '$key='$expected', but was '$key=$actual' (${request.url})")
    }

    private fun assertUrlParamNotNull(request: Request, key: String) {
        assertNotNull(request.url.queryParameter(key), "Expected param $key, but was null (${request.url})")
    }
}
