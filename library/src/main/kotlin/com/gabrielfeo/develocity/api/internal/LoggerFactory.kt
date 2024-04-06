package com.gabrielfeo.develocity.api.internal

import com.gabrielfeo.develocity.api.Config
import org.slf4j.Logger
import java.io.File
import kotlin.reflect.KClass

interface LoggerFactory {
    fun newLogger(cls: KClass<*>): Logger
}

class RealLoggerFactory(
    private val config: Config,
) : LoggerFactory {

    private val loggingConfig by lazy {
        val level = checkNotNull(config.logLevel)
        File.createTempFile("develocity-api-kotlin-logging-config", ".xml").apply {
            writeText(LOGGING_CONFIG_TEMPLATE.replace("{{{LEVEL}}}", level))
        }
    }

    override fun newLogger(cls: KClass<*>): Logger {
        config.logLevel?.let {
            System.setProperty("logback.configurationFile", loggingConfig.absolutePath)
        }
        return org.slf4j.LoggerFactory.getLogger(cls.java)
    }
}

private const val LOGGING_CONFIG_TEMPLATE = """
<configuration>
	<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
		<!-- encoders are assigned the type
			 ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
		<encoder>
			<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} -%kvp- %msg%n</pattern>
		</encoder>
	</appender>

	<root level="{{{LEVEL}}}">
		<appender-ref ref="STDOUT" />
	</root>
</configuration>
"""
