package com.gabrielfeo.develocity.api.internal

internal var systemProperties: SystemProperties = RealSystemProperties

internal interface SystemProperties {
    val userHome: String?
}

internal object RealSystemProperties : SystemProperties {
    override val userHome: String? = System.getProperty("user.home")
}
