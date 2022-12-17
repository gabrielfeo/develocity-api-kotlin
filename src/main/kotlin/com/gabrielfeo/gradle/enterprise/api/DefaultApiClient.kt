package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.infrastructure.ApiClient

val defaultApiClient = ApiClient(
    baseUrl = requireBaseUrl(),
    bearerToken = requireToken(),
    authName = "GradleEnterpriseAccessKey",
)
