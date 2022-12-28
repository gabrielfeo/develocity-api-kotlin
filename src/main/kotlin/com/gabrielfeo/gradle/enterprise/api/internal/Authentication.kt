package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.debugLoggingEnabled
import java.util.logging.Level.INFO
import java.util.logging.Logger

private const val DEFAULT_KEY_NAME = "gradle-enterprise-api-token"
private const val DEFAULT_VAR_NAME = "GRADLE_ENTERPRISE_API_TOKEN"

internal fun requireToken(
    keyName: String = DEFAULT_KEY_NAME,
    varName: String = DEFAULT_VAR_NAME,
): String {
    return tokenFromKeychain(keyName)
        ?: tokenFromEnv(varName)
        ?: error("""
            No API token. Either
              - create a key in macOS keychain labeled $DEFAULT_KEY_NAME
              - export in environment variable $DEFAULT_VAR_NAME
              - set the global property `accessToken`
        """.trimIndent())
}

private fun tokenFromEnv(varName: String): String? {
    return System.getenv(varName).also {
        if (debugLoggingEnabled && it.isNullOrBlank()) {
            Logger.getGlobal().log(INFO, "Env var $varName=$it")
        }
    }
}

private fun tokenFromKeychain(keyName: String): String? {
    val login = System.getenv("LOGNAME")
    val process = ProcessBuilder(
        "security", "find-generic-password", "-w", "-a", login, "-s", keyName
    ).start()
    val status = process.waitFor()
    if (status == 0) {
        return process.inputStream.bufferedReader().use {
            it.readText().trim()
        }
    } else if (debugLoggingEnabled) {
        Logger.getGlobal().log(INFO, "Failed to get key from keychain (exit $status)")
    }
    return null
}
