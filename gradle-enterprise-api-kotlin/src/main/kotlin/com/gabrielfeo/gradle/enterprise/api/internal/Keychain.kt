package com.gabrielfeo.gradle.enterprise.api.internal

internal var keychain: Keychain = RealKeychain(systemProperties)

internal interface Keychain {
    fun get(entry: String): KeychainResult
}

internal sealed interface KeychainResult {
    data class Success(val token: String) : KeychainResult
    data class Error(val description: String) : KeychainResult
}

internal class RealKeychain(
    private val systemProperties: SystemProperties,
) : Keychain {
    override fun get(
        entry: String,
    ): KeychainResult {
        val login = systemProperties["user.name"] ?:
            return KeychainResult.Error("null user.name")
        val process = ProcessBuilder(
            "security", "find-generic-password", "-w", "-a", login, "-s", entry
        ).start()
        val status = process.waitFor()
        if (status != 0) {
            return KeychainResult.Error("exit $status")
        }
        val token = process.inputStream.bufferedReader().use {
            it.readText().trim()
        }
        return KeychainResult.Success(token)
    }
}
