package com.gabrielfeo.develocity.api.internal

import com.gabrielfeo.develocity.api.Config
import kotlin.reflect.KClass

internal class ProxyLoggerFactory(
    private val delegate: LoggerFactory,
) : LoggerFactory {

    val createdLoggers: MutableList<org.slf4j.Logger> = mutableListOf()

    override fun newLogger(cls: KClass<*>) = delegate.newLogger(cls)
        .also { createdLoggers.add(it) }

    override fun newLogger(name: String) = delegate.newLogger(name)
        .also { createdLoggers.add(it) }
}
