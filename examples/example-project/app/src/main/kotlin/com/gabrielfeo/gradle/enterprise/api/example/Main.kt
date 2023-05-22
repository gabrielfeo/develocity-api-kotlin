package com.gabrielfeo.gradle.enterprise.api.example

import com.gabrielfeo.gradle.enterprise.api.Config
import com.gabrielfeo.gradle.enterprise.api.GradleEnterpriseApi
import com.gabrielfeo.gradle.enterprise.api.example.analysis.mostFrequentBuilds
import okhttp3.OkHttpClient

/*
 * Example main that runs all API analysis at once. In projects, you can share an
 * OkHttpClient.Builder between GradleEnterpriseApi and your own project classes, in order to
 * save resources.
 */

val clientBuilder = OkHttpClient.Builder()

suspend fun main() {
    val newConfig = Config(
        clientBuilder = clientBuilder,
    )
    val gradleEnterpriseApi = GradleEnterpriseApi.newInstance(newConfig)
    runAllAnalysis(gradleEnterpriseApi)
    gradleEnterpriseApi.shutdown()
}

private suspend fun runAllAnalysis(gradleEnterpriseApi: GradleEnterpriseApi) {
    mostFrequentBuilds(api = gradleEnterpriseApi.buildsApi)
}
