package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.*
import com.gabrielfeo.develocity.api.internal.auth.AccessKeyResolver
import com.gabrielfeo.develocity.api.internal.auth.accessKeyResolver
import com.squareup.moshi.Moshi
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import retrofit2.Retrofit
import kotlin.test.*

class RetrofitTest {

    @Test
    fun `Sets instance URL from options, stripping api segment`() {
        val retrofit = buildRetrofit(
            "DEVELOCITY_API_URL" to "https://example.com/api/",
        )
        // That's what generated classes expect
        assertEquals("https://example.com/", retrofit.baseUrl().toString())
    }

    @Test
    fun `Rejects invalid URL`() {
        assertFails {
            buildRetrofit(
                "DEVELOCITY_API_URL" to "https://example.com/",
            )
        }
    }

    private fun buildRetrofit(
        vararg envVars: Pair<String, String?>,
    ): Retrofit {
        val fakeEnv = FakeEnv(*envVars)
        if ("DEVELOCITY_ACCESS_KEY" !in fakeEnv)
            fakeEnv["DEVELOCITY_ACCESS_KEY"] = "example.com=example-token"
        if ("DEVELOCITY_API_URL" !in fakeEnv)
            fakeEnv["DEVELOCITY_API_URL"] = "https://example.com/api/"
        env = fakeEnv
        systemProperties = FakeSystemProperties()
        accessKeyResolver = AccessKeyResolver(
            env,
            homeDirectory = "/home/testuser".toPath(),
            fileSystem = FakeFileSystem(),
        )
        val config = Config()
        return buildRetrofit(
            config = config,
            client = buildOkHttpClient(config, RealLoggerFactory(config)),
            moshi = Moshi.Builder().build()
        )
    }
}
