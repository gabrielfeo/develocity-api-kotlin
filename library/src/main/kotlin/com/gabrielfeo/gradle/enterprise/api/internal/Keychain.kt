package com.gabrielfeo.develocity.api.internal

import com.gabrielfeo.develocity.api.Config
import org.slf4j.Logger

internal var keychain: Keychain = realKeychain()
internal fun realKeychain() = RealKeychain(
    RealSystemProperties,
    // Setting level via env will work, via code won't. Not worth fixing, since keychain will
    // be removed soon.
    RealLoggerFactory(Config()).newLogger(RealKeychain::class),
)

internal interface Keychain {
    fun get(entry: String): KeychainResult
}

internal sealed interface KeychainResult {
    data class Success(val token: String) : KeychainResult
    data class Error(val description: String) : KeychainResult
}

internal class RealKeychain(
    private val systemProperties: SystemProperties,
    private val logger: Logger,
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
        logger.debug("Keychain exit status: $status)")
        if (status != 0) {
            return KeychainResult.Error("exit $status")
        }
        println(KEYCHAIN_DEPRECATION_WARNING)
        val token = process.inputStream.bufferedReader().use {
            it.readText().trim()
        }
        return KeychainResult.Success(token)
    }
}

private const val KEYCHAIN_DEPRECATION_WARNING =
    "WARNING: passing token via macOS keychain is deprecated. Please pass it as the " +
        "GRADLE_ENTERPRISE_API_TOKEN environment variable instead. Keychain support will be " +
        "removed in the next release. See release notes for details and alternatives."
