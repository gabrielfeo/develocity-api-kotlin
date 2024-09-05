package com.gabrielfeo.develocity.api.internal

internal var systemProperties: SystemProperties = RealSystemProperties

internal interface SystemProperties {
    val userHome: String?
    val logLevel: String?
}

internal object RealSystemProperties : SystemProperties {
    override val userHome: String? = System.getProperty("user.home")
    override val logLevel: String? = System.getProperty(RealLoggerFactory.LOG_LEVEL_SYSTEM_PROPERTY)
}
