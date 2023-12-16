package com.gabrielfeo.gradle.enterprise.api.extension

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.internal.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class BuildsApiExtensionsIntegrationTest {

    @Test
    fun getBuildsFlowUsesQueryInAllRequests() = runTest {
        env = RealEnv
        keychain = RealKeychain(RealSystemProperties)
        val recorder = RequestRecorder()
        val api = buildApi(recorder)
        api.buildsApi.getBuildsFlow(since = 0, query = "user:*").take(2000).collect()
        recorder.requests.forEachIndexed { i, request ->
            assertEquals("user:*", request.url.queryParameter("query"), "[$i]")
        }
        api.shutdown()
    }

    private fun buildApi(recorder: RequestRecorder) =
        GradleEnterpriseApi.newInstance(
            config = Config(
                clientBuilder = recorder.clientBuilder(),
                cacheConfig = Config.CacheConfig(cacheEnabled = false),
            )
        )
}