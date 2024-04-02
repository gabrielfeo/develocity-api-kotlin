package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.internal.keychain
import kotlin.test.*

internal class KeychainIntegrationTest {

    @Test
    fun getApiToken() {
        env = RealEnv
        keychain = realKeychain()
        val result = keychain.get("gradle-enterprise-api-token")
        assertIs<KeychainResult.Success>(result)
        assertFalse(result.token.isNullOrBlank(), "Keychain returned null or blank")
    }
}
