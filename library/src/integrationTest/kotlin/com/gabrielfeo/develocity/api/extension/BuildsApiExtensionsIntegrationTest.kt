package com.gabrielfeo.develocity.api.extension

import com.gabrielfeo.develocity.api.*
import com.gabrielfeo.develocity.api.internal.*
import com.gabrielfeo.develocity.api.model.BuildModelName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.test.runTest
import okhttp3.Request
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import kotlin.test.AfterTest
import kotlin.time.Duration.Companion.minutes

class BuildsApiExtensionsIntegrationTest {

    init {
        env = RealEnv
    }

    private val recorder = RequestRecorder()
    private val api = buildApi(recorder)

    @AfterTest
    fun setup() {
        api.shutdown()
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
        ).take(4).printProgress(0..4 step 2).collect()
        recorder.requests.forEach {
            assertUrlParam(it, "query", "user:*")
            assertUrlParam(it, "models", "gradle-attributes")
            assertUrlParam(it, "allModels", "true")
            assertUrlParam(it, "reverse", "true")
        }
    }

    @Test
    fun getBuildsFlowReplacesSinceForFromBuildAfterFirstRequest() = runTest {
        api.buildsApi.getBuildsFlow(since = 1, buildsPerPage = 2)
            .take(10)
            .printProgress(0..10 step 2)
            .collect()
        assertReplacedForFromBuildAfterFirstRequest(param = "since" to "1")
    }

    @Test
    fun getBuildsFlowReplacesFromInstantForFromBuildAfterFirstRequest() = runTest {
        api.buildsApi.getBuildsFlow(fromInstant = 1, buildsPerPage = 2)
            .take(10)
            .printProgress(0..10 step 2)
            .collect()
        assertReplacedForFromBuildAfterFirstRequest(param = "fromInstant" to "1")
    }

    fun <T> Flow<T>.printProgress(indices: IntProgression): Flow<T> {
        return withIndex().onEach { (i, _) ->
            if (i in indices && i % indices.step == 0) {
                println("PROGRESS: $i/${indices.last} builds")
            }
        }.map {
            it.value
        }
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
