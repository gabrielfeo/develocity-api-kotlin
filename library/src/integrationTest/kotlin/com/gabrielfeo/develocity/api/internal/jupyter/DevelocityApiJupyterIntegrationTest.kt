package com.gabrielfeo.develocity.api.internal.jupyter

import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.jupyter.api.Code
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import kotlin.test.Test

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

    private fun assertSucceeds(@Language("kts") code: Code) {
        code.lines().forEach {
            execRendered(it)
        }
    }
}