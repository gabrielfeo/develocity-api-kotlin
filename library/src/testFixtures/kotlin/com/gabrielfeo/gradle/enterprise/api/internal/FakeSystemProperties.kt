package com.gabrielfeo.gradle.enterprise.api.internal

class FakeSystemProperties(
    vararg vars: Pair<String, String?>,
) : SystemProperties {

    companion object {
        val macOs = FakeSystemProperties("os.name" to "Mac OS X")
        val linux = FakeSystemProperties("os.name" to "Linux")
    }

    private val vars = vars.toMap(HashMap())

    override fun get(name: String) = vars[name]

    operator fun set(name: String, value: String?) = vars.put(name, value)
    operator fun contains(name: String) = name in vars
}