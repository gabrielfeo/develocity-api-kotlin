package com.gabrielfeo.gradle.enterprise.api.internal

import ch.qos.logback.classic.Level
import com.gabrielfeo.gradle.enterprise.api.Config
import org.slf4j.Logger
import kotlin.reflect.KClass

interface LoggerFactory {
    fun newLogger(cls: KClass<*>): Logger
}

class RealLoggerFactory(
    private val config: Config,
) : LoggerFactory {

    override fun newLogger(cls: KClass<*>): Logger {
        val impl = org.slf4j.LoggerFactory.getLogger(cls.java) as ch.qos.logback.classic.Logger
        return impl.apply {
            level = Level.valueOf(config.logLevel)
        }
    }
}
