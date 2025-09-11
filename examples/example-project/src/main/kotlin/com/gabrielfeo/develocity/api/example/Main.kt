package com.gabrielfeo.develocity.api.example

import com.gabrielfeo.develocity.api.Config
import com.gabrielfeo.develocity.api.DevelocityApi
import com.gabrielfeo.develocity.api.example.analysis.mostFrequentBuilds
import okhttp3.OkHttpClient

/*
 * Example main that runs all API analysis at once. In projects, you can share an
 * OkHttpClient.Builder between DevelocityApi and your own project classes, in order to
 * save resources.
 */

val clientBuilder = OkHttpClient.Builder()

suspend fun main(args: Array<String>) {
    val newConfig = Config(
        clientBuilder = clientBuilder,
    )
    val develocityApi = DevelocityApi.newInstance(newConfig)
    val query = args.getOrElse(0) { "buildStartTime>-1d buildTool:gradle" }
    runAllAnalysis(develocityApi, query)
    develocityApi.shutdown()
}

private suspend fun runAllAnalysis(develocityApi: DevelocityApi, query: String) {
    mostFrequentBuilds(api = develocityApi.buildsApi, query)
}
