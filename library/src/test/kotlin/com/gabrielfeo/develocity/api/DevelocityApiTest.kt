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
        val error = assertThrows<Exception> {
            DevelocityApi.newInstance(Config())
        }
        error.assertRootMessageContains("DEVELOCITY_API_URL")
    }

    @Test
    fun `Fails lazily if no access key`() {
        env = FakeEnv("DEVELOCITY_API_URL" to "https://example.com/api/")
        val api = assertDoesNotThrow {
            DevelocityApi.newInstance(Config())
        }
        val error = assertThrows<Exception> {
            api.buildsApi.toString()
        }
        error.assertRootMessageContains("DEVELOCITY_ACCESS_KEY")
    }

    private fun Throwable.assertRootMessageContains(text: String) {
        cause?.assertRootMessageContains(text) ?: assertContains(message.orEmpty(), text)
    }
}
