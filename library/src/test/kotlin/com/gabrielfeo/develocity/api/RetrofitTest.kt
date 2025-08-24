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
    fun `Sets instance URL from options, appending api segment`() {
        val retrofit = buildRetrofit(
            "DEVELOCITY_URL" to "https://example.com/",
        )
        // The baseUrl should be the API endpoint
        assertEquals("https://example.com/api/", retrofit.baseUrl().toString())
    }

    private fun buildRetrofit(
        vararg envVars: Pair<String, String?>,
    ): Retrofit {
        val fakeEnv = FakeEnv(*envVars)
        if ("DEVELOCITY_ACCESS_KEY" !in fakeEnv)
            fakeEnv["DEVELOCITY_ACCESS_KEY"] = "example.com=example-token"
        if ("DEVELOCITY_URL" !in fakeEnv)
            fakeEnv["DEVELOCITY_URL"] = "https://example.com/"
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
