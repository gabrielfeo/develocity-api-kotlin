package com.gabrielfeo.gradle.enterprise.api.internal

private const val DEFAULT_KEY_NAME = "gradle-enterprise-api-token"
private const val DEFAULT_VAR_NAME = "GRADLE_ENTERPRISE_API_TOKEN"

internal fun requireToken(
    keyName: String = DEFAULT_KEY_NAME,
    varName: String = DEFAULT_VAR_NAME,
    debugging: Boolean = false,
): String {
    return tokenFromKeychain(keyName, debugging)
        ?: tokenFromEnv(varName, debugging)
        ?: error("""
            No API token. Either
              - create a key in macOS keychain labeled $DEFAULT_KEY_NAME
              - export in environment variable $DEFAULT_VAR_NAME
              - set the global property `accessToken`
        """.trimIndent())
}

private fun tokenFromEnv(varName: String, debugging: Boolean): String? {
    return System.getenv(varName).also {
        if (debugging && it.isNullOrBlank()) {
            println("Env var $varName=$it")
        }
    }
}

private fun tokenFromKeychain(keyName: String, debugging: Boolean): String? {
    val login = System.getenv("LOGNAME")
    val process = ProcessBuilder(
        "security", "find-generic-password", "-w", "-a", login, "-s", keyName
    ).start()
    val status = process.waitFor()
    if (status == 0) {
        return process.inputStream.bufferedReader().use {
            it.readText().trim()
        }
    } else if (debugging) {
        println("Failed to get key from keychain (exit $status)")
    }
    return null
}
