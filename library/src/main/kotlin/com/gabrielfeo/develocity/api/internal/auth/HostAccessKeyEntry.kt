package com.gabrielfeo.develocity.api.internal.auth

internal class HostAccessKeyEntry(entry: String) {

    private val components = entry.split('=')

    init {
        require(components.size == 2 && host.isNotBlank() && accessKey.isNotBlank()) {
            val redactedEntry = if (entry.length <= 5) entry else "${entry.substring(0, 4)}[redacted]"
            "Invalid access key entry format: '$redactedEntry'. Expected format is 'host=accessKey'."
        }
    }

    val host: String get() = components[0]
    val accessKey: String get() = components[1]
}