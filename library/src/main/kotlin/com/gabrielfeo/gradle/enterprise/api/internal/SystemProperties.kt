package com.gabrielfeo.gradle.enterprise.api.internal

internal var systemProperties: SystemProperties = RealSystemProperties

internal interface SystemProperties {
    operator fun get(name: String): String?
}

internal object RealSystemProperties : SystemProperties {
    override fun get(name: String): String? = System.getProperty(name)
}
