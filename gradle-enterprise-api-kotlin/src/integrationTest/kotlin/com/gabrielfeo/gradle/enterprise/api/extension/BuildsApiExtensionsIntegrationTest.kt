package com.gabrielfeo.gradle.enterprise.api.extension

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.internal.*
import com.gabrielfeo.gradle.enterprise.api.model.BuildModelName
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import okhttp3.Request
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import kotlin.test.AfterTest
import kotlin.time.Duration.Companion.minutes

class BuildsApiExtensionsIntegrationTest {

    init {
        env = RealEnv
        keychain = realKeychain()
    }

    private val recorder = RequestRecorder()
    private val api = buildApi(recorder)

    @AfterTest
    fun setup() {
        api.shutdown()
    }

    @Test
    fun getBuildsFlowPreservesParamsAcrossRequests() = runTest(timeout = 3.minutes) {
        api.buildsApi.getBuildsFlow(
            since = 0,
            query = "user:*",
            models = listOf(BuildModelName.gradleAttributes),
            reverse = true,
        ).take(2000).collect()
        recorder.requests.forEach {
            assertUrlParam(it, "query", "user:*")
            assertUrlParam(it, "models", "gradle-attributes")
            assertUrlParam(it, "reverse", "true")
        }
    }

    @Test
    fun getBuildsFlowReplacesSinceForFromBuildAfterFirstRequest() = runTest {
        api.buildsApi.getBuildsFlow(since = 1).take(2000).collect()
        assertReplacedForFromBuildAfterFirstRequest(param = "since" to "1")
    }

    @Test
    fun getBuildsFlowReplacesFromInstantForFromBuildAfterFirstRequest() = runTest {
        api.buildsApi.getBuildsFlow(fromInstant = 1).take(2000).collect()
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
        GradleEnterpriseApi.newInstance(
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