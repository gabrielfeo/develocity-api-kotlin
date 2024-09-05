package com.gabrielfeo.develocity.api.internal

import com.gabrielfeo.develocity.api.Config
import org.slf4j.Logger
import kotlin.reflect.KClass

interface LoggerFactory {
    fun newLogger(cls: KClass<*>): Logger
}

class RealLoggerFactory(
    private val config: Config,
) : LoggerFactory {

    override fun newLogger(cls: KClass<*>): Logger {
        setLogLevel()
        return org.slf4j.LoggerFactory.getLogger(cls.java)
    }

    private fun setLogLevel() {
        System.setProperty(LOG_LEVEL_SYSTEM_PROPERTY, config.logLevel)
    }

    companion object {
        const val LOG_LEVEL_SYSTEM_PROPERTY = "org.slf4j.simpleLogger.defaultLogLevel"
    }
}
