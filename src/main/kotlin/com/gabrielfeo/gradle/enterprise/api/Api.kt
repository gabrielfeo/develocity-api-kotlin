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
 * Shutdown the internal client, releasing resources and allowing the program to
 * finish before the client's idle timeout.
 *
 * https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/#shutdown-isnt-necessary
 */
fun shutdown() {
    okHttpClient.run {
        dispatcher.executorService.shutdown()
        connectionPool.evictAll();
        cache?.close();
    }
}
