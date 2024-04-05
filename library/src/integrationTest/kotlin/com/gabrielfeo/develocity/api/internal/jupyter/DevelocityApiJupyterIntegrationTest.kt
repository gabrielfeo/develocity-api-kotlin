package com.gabrielfeo.develocity.api.internal.jupyter

import com.gabrielfeo.develocity.api.DevelocityApi
import com.google.common.reflect.ClassPath
import com.google.common.reflect.ClassPath.ClassInfo
import kotlinx.coroutines.test.runTest
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.jupyter.api.Code
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.javaType
import kotlin.test.Test

@ExperimentalStdlibApi
class DevelocityApiJupyterIntegrationTest : JupyterReplTestCase() {

    @Test
    fun `imports main package`() = assertSucceeds("""
        DevelocityApi::class
        Config::class
    """)

    @Test
    fun `imports models package`() = assertSucceeds("""
        Build::class
        GradleAttributes::class
    """)

    @Test
    fun `imports extensions package`() = assertSucceeds("""
        com.gabrielfeo.develocity.api.BuildsApi::getBuildsFlow
    """)

    @Test
    fun `imports all library classes`() {
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