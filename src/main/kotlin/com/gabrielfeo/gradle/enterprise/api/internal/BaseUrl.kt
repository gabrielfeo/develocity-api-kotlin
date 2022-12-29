package com.gabrielfeo.gradle.enterprise.api.internal

internal fun requireBaseUrl(
    envName: String,
): String {
    return checkNotNull(System.getenv(envName)) {
        """
            No base URL provided. Either
              - export an environment variable `GRADLE_ENTERPRISE_BASE_URL`
              - set the global property `baseUrl`
        """.trimIndent()
    }
}
