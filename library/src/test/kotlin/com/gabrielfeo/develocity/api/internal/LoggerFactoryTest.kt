package com.gabrielfeo.develocity.api.internal

import kotlin.test.Test
import com.gabrielfeo.develocity.api.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class LoggerFactoryTest {

    private val logLevelProperty = RealLoggerFactory.LOG_LEVEL_SYSTEM_PROPERTY
    private val defaultLogLevelProperty = "org.slf4j.simpleLogger.defaultLogLevel"

    @BeforeTest
    @AfterTest
    fun cleanup() {
        System.clearProperty(logLevelProperty)
        System.clearProperty(defaultLogLevelProperty)
        env = FakeEnv("DEVELOCITY_URL" to "https://example.com/")
    }

    @Test
    fun `Given custom log level set, loggers created with log level`() {
        check(Config().logLevel != "trace") { "Precondition failed: default level is already trace" }
        val loggerFactory = RealLoggerFactory(Config(logLevel = "trace"))
        val logger = loggerFactory.newLogger(LoggerFactoryTest::class)
        assertTrue(logger.isTraceEnabled)
    }

    @Test
    fun `Given custom log level set, unrelated loggers unaffected`() {
        val unrelatedLogger = org.slf4j.LoggerFactory.getLogger("foo.Bar")
        check(!unrelatedLogger.isTraceEnabled) { "Precondition failed: unrelated logger is already trace" }
        val loggerFactory = RealLoggerFactory(Config(logLevel = "trace"))
        val logger = loggerFactory.newLogger(LoggerFactoryTest::class)
        assertTrue(logger.isTraceEnabled)
        assertFalse(unrelatedLogger.isTraceEnabled)
    }
}
