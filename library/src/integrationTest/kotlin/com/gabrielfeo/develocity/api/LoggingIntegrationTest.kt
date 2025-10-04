package com.gabrielfeo.develocity.api

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.LoggerContext as LogbackLoggerContext
import com.gabrielfeo.develocity.api.internal.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import org.slf4j.Logger
import org.slf4j.Logger.ROOT_LOGGER_NAME
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.test.*

class LoggingIntegrationTest {

    @TempDir
    lateinit var tempDir: File

    private val recorder: InMemoryLogRecorder by lazy {
        val lc = LoggerFactory.getILoggerFactory() as LogbackLoggerContext
        // The appender is attached to the com.gabrielfeo.develocity.api logger
        val logger = lc.getLogger("com.gabrielfeo.develocity")
        logger.getAppender("IN_MEMORY") as InMemoryLogRecorder
    }

    private lateinit var api: DevelocityApi

    @BeforeTest
    fun setup() {
        // Appender is configured via logback-test.xml; ensure it's started
        recorder.start()
        val mockWebServer = okhttp3.mockwebserver.MockWebServer()
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setBody("[]"))
        mockWebServer.start()
        env = FakeEnv()
        api = DevelocityApi.newInstance(
            config = Config(
                server = mockWebServer.url("/").toUri(),
                accessKey = { "${mockWebServer.url("/").host}=foo" },
                cacheConfig = Config.CacheConfig(
                    cacheEnabled = true,
                    cacheDir = tempDir,
                )
            )
        )
    }

    @AfterTest
    fun tearDown() {
        val lc = LoggerFactory.getILoggerFactory() as LogbackLoggerContext
        val logger = lc.getLogger("com.gabrielfeo.develocity")
        logger.detachAppender("IN_MEMORY")
        recorder.stop()
        api.shutdown()
    }

    @Test
    fun logsUnderLibraryPackage() = runTest {
        api.buildsApi.getBuilds(since = 0, maxBuilds = 1)
        with(recorder.logsByLoggerName) {
            assertTrue(isNotEmpty())
            assertTrue(any { (_, message) -> message.contains("cache dir", ignoreCase = true) })
            assertTrue(any { (_, message) -> message.contains("cache miss", ignoreCase = true) })
            assertTrue(any { (_, message) -> message.contains("get", ignoreCase = true) })
            forEach { (loggerName, message) ->
                assertTrue(
                    loggerName.startsWith("com.gabrielfeo.develocity.api"),
                    "Log from unexpected logger: '$loggerName' with message '$message'"
                )
            }
        }
    }
}
