package com.gabrielfeo.develocity.api.internal

import kotlin.test.Test
import com.gabrielfeo.develocity.api.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
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
    fun `Level always copied from Config`() {
        val loggerFactory = RealLoggerFactory(Config(logLevel = "foo"))
        loggerFactory.newLogger(LoggerFactoryTest::class)
        assertEquals("foo", System.getProperty(logLevelProperty))
    }

    @Test
    fun `Level has no effect on other loggers`() {
        val loggerFactory = RealLoggerFactory(Config(logLevel = "debug"))
        val logger = loggerFactory.newLogger(LoggerFactoryTest::class)
        val otherLogger = org.slf4j.LoggerFactory.getLogger("foo.Bar")
        assertTrue(logger.isDebugEnabled)
        assertFalse(otherLogger.isDebugEnabled)
    }
}
