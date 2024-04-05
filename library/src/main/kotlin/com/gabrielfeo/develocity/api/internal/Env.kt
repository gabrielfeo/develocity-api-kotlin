package com.gabrielfeo.develocity.api.internal

internal var env: Env = RealEnv

internal interface Env {
    operator fun get(name: String): String?
}

internal object RealEnv : Env {
    override fun get(name: String): String? = System.getenv(name)
}
