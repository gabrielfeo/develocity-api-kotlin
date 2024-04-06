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
        if (System.getProperty(SIMPLE_LOGGER_LOG_LEVEL) == null) {
            System.setProperty(SIMPLE_LOGGER_LOG_LEVEL, config.logLevel ?: "off")
        }
        return org.slf4j.LoggerFactory.getLogger(cls.java)
    }
}

private const val SIMPLE_LOGGER_LOG_LEVEL = "org.slf4j.simpleLogger.defaultLogLevel"
