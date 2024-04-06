package com.gabrielfeo.develocity.api.internal

import kotlin.test.Test
import com.gabrielfeo.develocity.api.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class LoggerFactoryTest {

    @BeforeTest
    @AfterTest
    fun cleanup() {
        System.clearProperty(SIMPLE_LOGGER_LOG_LEVEL)
        env = FakeEnv("DEVELOCITY_API_URL" to "https://example.com/")
    }

    @Test
    fun `Logging off by default`() {
        val loggerFactory = RealLoggerFactory(Config())
        loggerFactory.newLogger(LoggerFactoryTest::class)
        assertEquals("off", System.getProperty(SIMPLE_LOGGER_LOG_LEVEL))
    }

    @Test
    fun `simple-logger property unchanged if already set`() {
        val loggerFactory = RealLoggerFactory(Config(logLevel = "bar"))
        System.setProperty(SIMPLE_LOGGER_LOG_LEVEL, "foo")
        loggerFactory.newLogger(LoggerFactoryTest::class)
        assertEquals("foo", System.getProperty(SIMPLE_LOGGER_LOG_LEVEL))
    }

    @Test
    fun `newLogger should return a logger with the correct log level`() {
        val loggerFactory = RealLoggerFactory(Config(logLevel = "foo"))
        loggerFactory.newLogger(LoggerFactoryTest::class)
        assertEquals("foo", System.getProperty(SIMPLE_LOGGER_LOG_LEVEL))
    }
}
