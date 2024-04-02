package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.*
import com.google.common.reflect.ClassPath
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.memberProperties
import kotlin.reflect.javaType
import kotlin.test.*

@OptIn(ExperimentalStdlibApi::class)
class GradleEnterpriseApiIntegrationTest {

    @Test
    fun canFetchBuildsWithDefaultConfig() = runTest {
        env = RealEnv
        keychain = realKeychain()
        val api = GradleEnterpriseApi.newInstance(
            config = Config(
                cacheConfig = Config.CacheConfig(cacheEnabled = false)
            )
        )
        val builds = api.buildsApi.getBuilds(
            since = 0,
            maxBuilds = 5,
            query = """tag:local value:"Email=gabriel.feo*""""
        )
        assertEquals(5, builds.size)
        api.shutdown()
    }

    @Test
    fun canBuildNewInstanceWithPureCodeConfiguration() = runTest {
        env = FakeEnv()
        keychain = FakeKeychain()
        assertDoesNotThrow {
            val config = Config(
                apiUrl = "https://google.com/api/",
                apiToken = { "" },
            )
            GradleEnterpriseApi.newInstance(config)
        }
    }

    @Test
    fun mainApiInterfaceExposesAllGeneratedApiClasses() = runTest {
        val generatedApiTypes = getGeneratedApiTypes()
        val mainApiInterfaceProperties = getMainApiInterfaceProperties()
        generatedApiTypes.forEach {
            mainApiInterfaceProperties.singleOrNull { type -> type == it }
                ?: fail("No property in GradleEnterpriseApi for $it")
        }
    }

    private fun getGeneratedApiTypes(): List<String> {
        val cp = ClassPath.from(this::class.java.classLoader)
        return cp.getTopLevelClasses("com.gabrielfeo.gradle.enterprise.api")
            .filter { it.simpleName.endsWith("Api") }
            .filter { !it.simpleName.endsWith("GradleEnterpriseApi") }
            .map { it.name }
    }

    private fun getMainApiInterfaceProperties() = GradleEnterpriseApi::class.memberProperties
        .filter { it.visibility == PUBLIC }
        .map { it.returnType.javaType.typeName }
}