package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.*
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertContains

class DevelocityApiTest {

    @Test
    fun `Fails eagerly if no API URL`() {
        env = FakeEnv()
        keychain = FakeKeychain()
        systemProperties = FakeSystemProperties.linux
        val error = assertThrows<Exception> {
            DevelocityApi.newInstance(Config())
        }
        error.assertRootMessageContains("DEVELOCITY_API_URL")
    }

    @Test
    fun `Fails lazily if no API token`() {
        env = FakeEnv("DEVELOCITY_API_URL" to "example-url")
        keychain = FakeKeychain()
        systemProperties = FakeSystemProperties.linux
        val api = assertDoesNotThrow {
            DevelocityApi.newInstance(Config())
        }
        val error = assertThrows<Exception> {
            api.buildsApi.toString()
        }
        error.assertRootMessageContains("DEVELOCITY_API_TOKEN")
    }

    private fun Throwable.assertRootMessageContains(text: String) {
        cause?.assertRootMessageContains(text) ?: assertContains(message.orEmpty(), text)
    }
}
