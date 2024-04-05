package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.*
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import kotlin.test.*

class RetrofitTest {

    @Test
    fun `Sets instance URL from options, stripping api segment`() {
        val retrofit = buildRetrofit(
            "GRADLE_ENTERPRISE_API_URL" to "https://example.com/api/",
        )
        // That's what generated classes expect
        assertEquals("https://example.com/", retrofit.baseUrl().toString())
    }

    @Test
    fun `Rejects invalid URL`() {
        assertFails {
            buildRetrofit(
                "GRADLE_ENTERPRISE_API_URL" to "https://example.com/",
            )
        }
    }

    private fun buildRetrofit(
        vararg envVars: Pair<String, String?>,
    ): Retrofit {
        val fakeEnv = FakeEnv(*envVars)
        if ("GRADLE_ENTERPRISE_API_TOKEN" !in fakeEnv)
            fakeEnv["GRADLE_ENTERPRISE_API_TOKEN"] = "example-token"
        env = fakeEnv
        systemProperties = FakeSystemProperties.macOs
        keychain = FakeKeychain()
        val config = Config()
        return buildRetrofit(
            config = config,
            client = buildOkHttpClient(config, RealLoggerFactory(config)),
            moshi = Moshi.Builder().build()
        )
    }
}
