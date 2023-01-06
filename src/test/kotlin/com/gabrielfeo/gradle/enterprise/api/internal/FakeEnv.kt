package com.gabrielfeo.gradle.enterprise.api.internal

class FakeEnv(
    vararg vars: Pair<String, String>,
) : Env {

    private val vars = vars.toMap()

    override fun get(name: String) = vars[name]
}
