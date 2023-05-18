package com.gabrielfeo.gradle.enterprise.api.internal

import kotlin.test.Test
import kotlin.test.assertFalse

class KeychainIntegrationTest {

    val keychain = RealKeychain(RealSystemProperties)

    @Test
    fun getApiToken() {
        assertFalse(
            keychain["gradle-enterprise-api-token"].isNullOrEmpty(),
            "Keychain returned null or empty",
        )
    }
}
