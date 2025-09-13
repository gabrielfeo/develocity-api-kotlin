package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.*
import com.google.common.reflect.ClassPath
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.assertDoesNotThrow
import java.net.URI
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.memberProperties
import kotlin.reflect.javaType
import kotlin.test.*

@OptIn(ExperimentalStdlibApi::class)
class DevelocityApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var mockWebServerEnv: Env
    private val emptyEnv: Env = FakeEnv()

    @BeforeTest
    fun setUp() {
        mockWebServer = MockWebServer().apply { start() }
        mockWebServerEnv = FakeEnv(
            "DEVELOCITY_URL" to mockWebServer.url("/").toString(),
            "DEVELOCITY_ACCESS_KEY" to "${mockWebServer.url("/").host}=foo",
            "DEVELOCITY_CACHE_ENABLED" to "false",
        )
    }

    @AfterTest
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun canFetchBuildsWithEnvVarConfigAndEmptyBuildsResponse() = runTest {
        env = mockWebServerEnv
        mockWebServer.enqueue(MockResponse().setBody("[]"))
        val api = DevelocityApi.newInstance()
        val builds = api.buildsApi.getBuilds(fromInstant = 0)
        assertEquals(0, builds.size)
        api.shutdown()
    }

    @Test
    fun canFetchBuildsWithEnvVarConfigAndNonEmptyBuildsResponse() = runTest {
        val response = requireResource("/response/api/builds/5-builds.json").readText()
        mockWebServer.enqueue(MockResponse().setBody(response))
        env = mockWebServerEnv
        val api = DevelocityApi.newInstance()
        val builds = api.buildsApi.getBuilds(fromInstant = 0)
        assertContentEquals(
            listOf(
                "67b3o5ld6iwc2",
                "e2bajrtqpe4bi",
                "rb5bbp6hxpcto",
                "gur3efx4fnqsc",
                "tw3yw5fhovwtq",
            ),
            builds.map { it.id },
        )
        api.shutdown()
    }

    @Test
    fun canFetchBuildsWithCodeConfig() = runTest {
        env = emptyEnv
        mockWebServer.enqueue(MockResponse().setBody("[]"))
        assertDoesNotThrow {
            val config = Config(
                server = mockWebServer.url("/").toUri(),
                accessKey = { "${mockWebServer.url("/").host}=foo" },
            )
            DevelocityApi.newInstance(config)
        }
    }

    @Test
    fun mainApiInterfaceExposesAllGeneratedApiClasses() = runTest {
        val generatedApiTypes = getGeneratedApiTypes()
        val mainApiInterfaceProperties = getMainApiInterfaceProperties()
        generatedApiTypes.forEach {
            mainApiInterfaceProperties.singleOrNull { type -> type == it }
                ?: fail("No property in DevelocityApi for $it")
        }
    }

    private fun getGeneratedApiTypes(): List<String> {
        val cp = ClassPath.from(this::class.java.classLoader)
        return cp.getTopLevelClasses("com.gabrielfeo.develocity.api")
            .filter { it.simpleName.endsWith("Api") }
            .filter { !it.simpleName.endsWith("DevelocityApi") }
            .map { it.name }
    }

    private fun getMainApiInterfaceProperties() = DevelocityApi::class.memberProperties
        .filter { it.visibility == PUBLIC }
        .map { it.returnType.javaType.typeName }
}
