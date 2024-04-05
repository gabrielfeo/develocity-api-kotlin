package com.gabrielfeo.develocity.api.internal.jupyter

import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration

@Suppress("unused")
class DevelocityApiJupyterIntegration : JupyterIntegration() {

    override fun Builder.onLoaded() {
        import("com.gabrielfeo.develocity.api.*")
        import("com.gabrielfeo.develocity.api.model.*")
        import("com.gabrielfeo.develocity.api.extension.*")
    }
}