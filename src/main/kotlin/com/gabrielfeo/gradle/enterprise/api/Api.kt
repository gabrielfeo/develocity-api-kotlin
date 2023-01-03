@file:Suppress("unused")

package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.*
import java.io.File
import kotlin.time.Duration.Companion.days

/**
 * The global instance of [GradleEnterpriseApi].
 */
val api: GradleEnterpriseApi by lazy {
    retrofit.create(GradleEnterpriseApi::class.java)
}

/**
 * Release resources allowing the program to finish before the internal client's idle timeout.
 */
fun shutdown() {
    okHttpClient.run {
        dispatcher.executorService.shutdown()
        connectionPool.evictAll();
        cache?.close();
    }
}
