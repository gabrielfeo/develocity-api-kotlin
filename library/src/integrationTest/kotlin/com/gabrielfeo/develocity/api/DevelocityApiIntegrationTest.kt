package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.*
import com.google.common.reflect.ClassPath
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertDoesNotThrow
import java.net.URI
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.memberProperties
import kotlin.reflect.javaType
import kotlin.test.*

@OptIn(ExperimentalStdlibApi::class)
class DevelocityApiIntegrationTest {

    // TODO Rename this test to canFetchBuildsWithDefaultConfigAndEnvVars
    // TODO Extract mockWebServer to class level for re-use between tests
    // TODO Change this test to use only environment variables from FakeEnv and no Config argument.
    @Test
    fun canFetchBuildsWithDefaultConfig() = runTest {
        val mockWebServer = okhttp3.mockwebserver.MockWebServer()
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setBody("[]"))
        mockWebServer.start()
        env = FakeEnv()
        val api = DevelocityApi.newInstance(
            config = Config(
                server = mockWebServer.url("/").toUri(),
                accessKey = { "${mockWebServer.url("/").host}=foo" },
                cacheConfig = Config.CacheConfig(cacheEnabled = false)
            )
        )
        val builds = api.buildsApi.getBuilds(
            since = 0,
            maxBuilds = 5,
            query = """buildStartTime>-7d""",
        )
        // Adjust assertion to match mocked response
        assertEquals(0, builds.size)
        api.shutdown()
        mockWebServer.shutdown()
    }

    // TODO Add a test case similar to above, but enqueuing a response with a real 5-build response JSON placed in resources and asserting those 5 were returned

    @Test
    fun canBuildNewInstanceWithPureCodeConfiguration() = runTest {
        env = FakeEnv()
        assertDoesNotThrow {
            val config = Config(
                server = URI("https://example.com/"),
                accessKey = { "example.com=example-token" }
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
