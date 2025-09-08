package com.gabrielfeo.develocity.api.internal

import com.gabrielfeo.develocity.api.Config
import org.slf4j.Logger
import kotlin.reflect.KClass

internal interface LoggerFactory {
    fun newLogger(cls: KClass<*>): Logger
    fun newLogger(name: String): Logger
}

internal class RealLoggerFactory(
    private val config: Config,
) : LoggerFactory {

    override fun newLogger(cls: KClass<*>): Logger {
        return org.slf4j.LoggerFactory.getLogger(cls.java)
    }

    override fun newLogger(name: String): Logger {
        return org.slf4j.LoggerFactory.getLogger(name)
    }


    companion object {
    }
}
