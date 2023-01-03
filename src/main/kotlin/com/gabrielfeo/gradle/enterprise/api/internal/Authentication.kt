package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.Options
import java.util.logging.Level.INFO
import java.util.logging.Logger

internal fun requireToken(
    keychainName: String,
    envName: String,
): String {
    return tokenFromKeychain(keychainName)
        ?: tokenFromEnv(envName)
        ?: error("""
            No API token found. Either
              - create a key in macOS keychain labeled `gradle-enterprise-api-token`
              - export in environment variable `GRADLE_ENTERPRISE_API_TOKEN`
              - set the global property `accessToken`
        """.trimIndent())
}

private fun tokenFromEnv(varName: String): String? {
    return System.getenv(varName).also {
        if (Options.debugLoggingEnabled && it.isNullOrBlank()) {
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
    } else if (Options.debugLoggingEnabled) {
        Logger.getGlobal().log(INFO, "Failed to get key from keychain (exit $status)")
    }
    return null
}
