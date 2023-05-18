package com.gabrielfeo.gradle.enterprise.api.example

import com.gabrielfeo.gradle.enterprise.api.example.analysis.percentDevelopersThatDontRunTestsLocally
import com.gabrielfeo.gradle.enterprise.api.gradleEnterpriseApi
import com.gabrielfeo.gradle.enterprise.api.options
import com.gabrielfeo.gradle.enterprise.api.shutdown
import okhttp3.OkHttpClient

/*
 * Example main that runs all API analysis at once. In projects, you can share an
 * OkHttpClient.Builder between GradleEnterpriseApi and your own project classes, in order to
 * save resources.
 */

val clientBuilder = OkHttpClient.Builder()

suspend fun main() {
    options.httpClient.clientBuilder = { clientBuilder }
    runAllAnalysis()
    shutdown()
}

private suspend fun runAllAnalysis() {
    percentDevelopersThatDontRunTestsLocally(api = gradleEnterpriseApi)
}
