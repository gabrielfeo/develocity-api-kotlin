package com.gabrielfeo.develocity.api.internal.auth

internal class HostAccessKeyEntry(entry: String) {

    private val components = entry.substringBefore(" #").trim().split('=')

    init {
        require(components.size == 2 && host.isNotBlank() && accessKey.isNotBlank()) {
            "Invalid access key entry format: '${redact(entry)}'. Expected format is 'host=accessKey'."
        }
    }

    val host: String get() = components[0]
    val accessKey: String get() = components[1]
}

private const val REDACTED_MAX_LENGTH = 5

private fun redact(entry: String): String =
    if (entry.length <= REDACTED_MAX_LENGTH) entry
    else "${entry.substring(0, REDACTED_MAX_LENGTH - 1)}[redacted]"
