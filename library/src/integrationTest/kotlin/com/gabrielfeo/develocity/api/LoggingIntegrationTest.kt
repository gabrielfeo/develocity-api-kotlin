package com.gabrielfeo.develocity.api

import ch.qos.logback.classic.Logger as LogbackLogger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
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
        (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as LogbackLogger).apply {
            detachAndStopAllAppenders()
            addAppender(recorder)
            addAppender(ConsoleAppender<ILoggingEvent>().apply {
                context = loggerContext
                encoder = PatternLayoutEncoder().apply {
                    context = loggerContext
                    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
                    start()
                }
                start()
            })
        }
        recorder.start()
        val mockWebServer = okhttp3.mockwebserver.MockWebServer()
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setBody("{\"builds\":[]}"))
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
