package com.gabrielfeo.gradle.enterprise.api.app.template

import com.gradle.enterprise.api.client.infrastructure.ApiClient

val defaultApiClient = ApiClient(
    baseUrl = requireBaseUrl(),
    bearerToken = requireToken(),
    authName = "GradleEnterpriseAccessKey",
)
