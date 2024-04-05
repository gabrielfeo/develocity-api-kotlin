package com.gabrielfeo.develocity.api.internal.jupyter

import com.google.common.reflect.ClassPath
import com.google.common.reflect.ClassPath.ClassInfo
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.jupyter.api.Code
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import kotlin.reflect.KVisibility
import kotlin.test.Test

@ExperimentalStdlibApi
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