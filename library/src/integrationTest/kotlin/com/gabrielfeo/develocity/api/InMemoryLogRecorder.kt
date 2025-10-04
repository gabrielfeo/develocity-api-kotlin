package com.gabrielfeo.develocity.api

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryLogRecorder : AppenderBase<ILoggingEvent>() {

    val logsByLoggerName: CopyOnWriteArrayList<Pair<String, String>> = CopyOnWriteArrayList()

    override fun append(eventObject: ILoggingEvent) {
        with(eventObject) {
            logsByLoggerName.add(loggerName to formattedMessage)
        }
    }

    fun clear() {
        logsByLoggerName.clear()
    }
}
