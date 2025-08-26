package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.*
import com.google.common.reflect.ClassPath
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertDoesNotThrow
import java.net.URL
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.memberProperties
import kotlin.reflect.javaType
import kotlin.test.*

@OptIn(ExperimentalStdlibApi::class)
class DevelocityApiIntegrationTest {

    @Test
    fun canFetchBuildsWithDefaultConfig() = runTest {
        env = RealEnv
        val api = DevelocityApi.newInstance(
            config = Config(
                cacheConfig = Config.CacheConfig(cacheEnabled = false)
            )
        )
        val builds = api.buildsApi.getBuilds(
            since = 0,
            maxBuilds = 5,
            query = """buildStartTime>-7d""",
        )
        assertEquals(5, builds.size)
        api.shutdown()
    }

    @Test
    fun canBuildNewInstanceWithPureCodeConfiguration() = runTest {
        env = FakeEnv()
        assertDoesNotThrow {
            val config = Config(
                server = URL("https://example.com/"),
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
