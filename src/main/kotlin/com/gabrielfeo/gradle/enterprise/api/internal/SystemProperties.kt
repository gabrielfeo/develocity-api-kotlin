package com.gabrielfeo.gradle.enterprise.api.internal

interface SystemProperties {
    operator fun get(name: String): String?
}

object RealSystemProperties : SystemProperties {
    override fun get(name: String): String? = System.getProperty(name)
}
