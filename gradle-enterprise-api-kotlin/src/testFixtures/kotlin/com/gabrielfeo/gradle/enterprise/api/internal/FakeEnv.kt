package com.gabrielfeo.gradle.enterprise.api.internal

class FakeEnv(
    vararg vars: Pair<String, String?>,
) : Env {

    private val vars = vars.toMap(HashMap())

    override fun get(name: String) = vars[name]

    operator fun set(name: String, value: String?) = vars.put(name, value)
    operator fun contains(name: String) = name in vars
}