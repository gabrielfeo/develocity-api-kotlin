package com.gabrielfeo.gradle.enterprise.api.internal

interface Env {
    operator fun get(name: String): String?
}

object RealEnv : Env {
    override fun get(name: String): String? = System.getenv(name)
}

fun Env.require(name: String) = checkNotNull(get(name)) { "'$name' variable required" }
