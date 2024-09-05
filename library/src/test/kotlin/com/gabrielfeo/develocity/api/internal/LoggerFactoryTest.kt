package com.gabrielfeo.develocity.api.internal

import kotlin.test.Test
import com.gabrielfeo.develocity.api.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class LoggerFactoryTest {

    private val logLevelProperty = RealLoggerFactory.LOG_LEVEL_SYSTEM_PROPERTY

    @BeforeTest
    @AfterTest
    fun cleanup() {
        System.clearProperty(logLevelProperty)
        env = FakeEnv("DEVELOCITY_API_URL" to "https://example.com/")
    }

    @Test
    fun `Level always copied from`() {
        val loggerFactory = RealLoggerFactory(Config(logLevel = "foo"))
        loggerFactory.newLogger(LoggerFactoryTest::class)
        assertEquals("foo", System.getProperty(logLevelProperty))
    }
}
