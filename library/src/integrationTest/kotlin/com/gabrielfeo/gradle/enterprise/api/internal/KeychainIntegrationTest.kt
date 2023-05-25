package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.internal.keychain
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class KeychainIntegrationTest {

    @Test
    fun getApiToken() {
        env = RealEnv
        keychain = RealKeychain(RealSystemProperties)
        val result = keychain.get("gradle-enterprise-api-token")
        assertInstanceOf(KeychainResult.Success::class.java, result)
        val success = result as KeychainResult.Success
        assertFalse(success.token.isNullOrBlank(), "Keychain returned null or blank")
    }
}
