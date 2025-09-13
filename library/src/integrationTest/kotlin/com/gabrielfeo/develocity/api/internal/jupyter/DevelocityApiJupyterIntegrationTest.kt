package com.gabrielfeo.develocity.api.internal.jupyter

import com.google.common.reflect.ClassPath
import com.google.common.reflect.ClassPath.ClassInfo
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.jupyter.api.Code
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import kotlin.reflect.KVisibility
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@ExperimentalStdlibApi
@Execution(CONCURRENT)
class DevelocityApiJupyterIntegrationTest : JupyterReplTestCase() {

    @Test
    fun `imports all extensions`() = assertSucceeds("""
        com.gabrielfeo.develocity.api.BuildsApi::getGradleAttributesFlow
        com.gabrielfeo.develocity.api.BuildsApi::getBuildsFlow

        val attrs = emptyList<com.gabrielfeo.develocity.api.model.BuildAttributesValue>()
        "custom value name" in attrs
        attrs["custom value name"]
    """)

    @Test
    fun `Given default clientBuilder, re-uses OkHttpClient resources`() {
        val config = """Config(server = java.net.URI("https://foo.com"), accessKey = { "foo.com=bar" })"""
        execSuccess("""val api = DevelocityApi.newInstance($config)""")
        execSuccess("""val api2 = DevelocityApi.newInstance($config)""")
        val connectionPool1 = execRendered("api.config.clientBuilder.build().connectionPool.hashCode()")
        val connectionPool2 = execRendered("api2.config.clientBuilder.build().connectionPool.hashCode()")
        assertEquals(connectionPool1, connectionPool2)
    }

    @Test
    fun `Given custom clientBuilder set, does not re-use OkHttpClient resources`() {
        val config = """
            Config(
                server = java.net.URI("https://foo.com"),
                accessKey = { "foo.com=bar" },
                clientBuilder = okhttp3.OkHttpClient.Builder(),
            )
        """.trimIndent()
        execSuccess("val api = DevelocityApi.newInstance($config)")
        execSuccess("val api2 = DevelocityApi.newInstance($config)")
        val connectionPool1 = execRendered("api.config.clientBuilder.build().connectionPool.hashCode()")
        val connectionPool2 = execRendered("api2.config.clientBuilder.build().connectionPool.hashCode()")
        assertNotEquals(connectionPool1, connectionPool2)
    }

    @Test
    fun `imports all public classes`() {
        val classes = allPublicClassesRecursive("com.gabrielfeo.develocity.api")
        val references = classes.joinToString("\n") { "${it.name}::class" }
        println("Running code:\n$references")
        assertSucceeds(references)
    }

    @Suppress("SameParameterValue")
    private fun allPublicClassesRecursive(packageName: String): List<ClassInfo> {
        val cp = ClassPath.from(this::class.java.classLoader)
        return cp.getTopLevelClassesRecursive(packageName)
            .filter { "internal" !in it.packageName }
            .filter { !it.name.endsWith("Kt") }
            .filter { Class.forName(it.name).kotlin.visibility == KVisibility.PUBLIC }
    }

    private fun assertSucceeds(@Language("kts") code: Code) {
        code.lines().forEach {
            execRendered(it)
        }
    }
}