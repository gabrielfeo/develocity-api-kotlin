package com.gabrielfeo.develocity.api.internal.jupyter

import org.jetbrains.kotlinx.jupyter.api.KotlinKernelVersion
import org.jetbrains.kotlinx.jupyter.api.libraries.LibraryDefinition

@Suppress("unused")
class DevelocityApiJupyterIntegration : LibraryDefinition {

    override val minKernelVersion = KotlinKernelVersion.from("0.12.0.217")

    override val imports = listOf(
        "com.gabrielfeo.develocity.api.*",
        "com.gabrielfeo.develocity.api.model.*",
        "com.gabrielfeo.develocity.api.extension.*",
    )
}
