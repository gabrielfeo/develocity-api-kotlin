package com.gabrielfeo.gradle.enterprise.api.app.template

import com.gradle.enterprise.api.client.infrastructure.ApiClient

sealed interface TokenSource {
    class MacOsKeychain(val keyName: String = "Gradle Enterprise API token") : TokenSource
    class EnvironmentVariable(val varName: String = "GRADLE_ENTERPRISE_API_TOKEN") : TokenSource
}

fun setAuthentication(source: TokenSource) {
    ApiClient.accessToken = when (source) {
        is TokenSource.MacOsKeychain -> getTokenFromKeychain(source.keyName)
        is TokenSource.EnvironmentVariable -> System.getenv(source.varName)
    }
}

private fun getTokenFromKeychain(keyName: String): String {
    val process = ProcessBuilder(
        "security",
        "find-generic-password",
        "-w",
        "-a",
        System.getenv("LOGNAME"),
        "-s",
        keyName
    ).start()
    val status = process.waitFor()
    check(status == 0) { "Failed to get key from keychain (exit $status)" }
    return process.inputStream.bufferedReader().use {
        it.readText().trim()
    }
}
