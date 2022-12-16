package com.gabrielfeo.gradle.enterprise.api.app.template

fun requireToken(
    keyName: String = "gradle-enterprise-api-token",
    varName: String = "GRADLE_ENTERPRISE_API_TOKEN",
    debugging: Boolean = false,
): String {
    return tokenFromKeychain(keyName, debugging)
        ?: tokenFromEnv(varName, debugging)
        ?: error("No API token. If your GE instance needs no auth, build a custom client.")
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
