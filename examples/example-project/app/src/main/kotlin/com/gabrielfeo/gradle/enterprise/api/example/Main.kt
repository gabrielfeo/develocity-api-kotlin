package com.gabrielfeo.gradle.enterprise.api.example

import com.gabrielfeo.gradle.enterprise.api.GradleEnterprise
import com.gabrielfeo.gradle.enterprise.api.example.analysis.mostFrequentBuilds
import okhttp3.OkHttpClient

/*
 * Example main that runs all API analysis at once. In projects, you can share an
 * OkHttpClient.Builder between GradleEnterpriseApi and your own project classes, in order to
 * save resources.
 */

val clientBuilder = OkHttpClient.Builder()

suspend fun main() {
    val newOptions = GradleEnterprise.options.copy(
        clientBuilder = clientBuilder,
    )
    val gradleEnterprise = GradleEnterprise.withOptions(newOptions)
    runAllAnalysis(gradleEnterprise)
    gradleEnterprise.shutdown()
}

private suspend fun runAllAnalysis(gradleEnterprise: GradleEnterprise) {
    mostFrequentBuilds(api = gradleEnterprise.api)
}
