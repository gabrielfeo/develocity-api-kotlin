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
        if (System.getProperty(SIMPLE_LOGGER_LOG_LEVEL) != null) {
            return
        }
        System.setProperty(SIMPLE_LOGGER_LOG_LEVEL, config.logLevel)
    }
}

internal const val SIMPLE_LOGGER_LOG_LEVEL = "org.slf4j.simpleLogger.defaultLogLevel"
