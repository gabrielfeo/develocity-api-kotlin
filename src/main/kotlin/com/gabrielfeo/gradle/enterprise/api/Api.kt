package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.infrastructure.ApiClient

var baseUrl: String? = null
var accessToken: String? = null

val api by lazy {
    val apiClient = ApiClient(
        baseUrl = baseUrl ?: requireBaseUrl(),
        bearerToken = accessToken ?: requireToken(),
        authName = "GradleEnterpriseAccessKey",
    )
    apiClient.createService(GradleEnterpriseApi::class.java)
}
