package com.gabrielfeo.develocity.api

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.LoggerContext as LogbackLoggerContext
import com.gabrielfeo.develocity.api.internal.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.test.*

class LoggingIntegrationTest {

    private class LogRecorder : AppenderBase<ILoggingEvent>() {
        val logsByLoggerName = mutableListOf<Pair<String, String>>()
        override fun append(eventObject: ILoggingEvent) {
            with(eventObject) {
                logsByLoggerName += (loggerName to formattedMessage)
            }
        }
    }

    @TempDir
    lateinit var tempDir: File

    private val recorder = LogRecorder()

    private lateinit var api: DevelocityApi

    @BeforeTest
    fun setup() {
        val loggerContext = LoggerFactory.getILoggerFactory() as LogbackLoggerContext
        val rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
        rootLogger.addAppender(recorder)
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
        (LoggerFactory.getILoggerFactory() as LogbackLoggerContext)
            .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
            .detachAndStopAllAppenders()
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
