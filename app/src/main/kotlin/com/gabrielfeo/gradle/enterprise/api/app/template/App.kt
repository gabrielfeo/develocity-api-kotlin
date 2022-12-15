package com.gabrielfeo.gradle.enterprise.api.app.template

import com.gradle.enterprise.api.GradleEnterpriseApi
import com.gradle.enterprise.api.client.infrastructure.ApiClient

fun main() {
    val apiClient = ApiClient(
        baseUrl = "https://gradle-enterprise.ifoodcorp.com.br",
        bearerToken = getTokenFromKeychain("ifood-ge-api-token"),
        authName = "GradleEnterpriseAccessKey",
    )
    val api = apiClient.createService(GradleEnterpriseApi::class.java)
    val builds = api.getBuilds(since = 0, maxBuilds = 3).execute().body()!!
    builds.forEach(::println)
}
