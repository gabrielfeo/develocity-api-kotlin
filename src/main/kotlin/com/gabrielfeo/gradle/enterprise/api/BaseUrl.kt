package com.gabrielfeo.gradle.enterprise.api

private const val DEFAULT_VAR_NAME = "GRADLE_ENTERPRISE_URL"

internal fun requireBaseUrl(
    varName: String = DEFAULT_VAR_NAME,
): String {
    return checkNotNull(System.getenv(varName)) {
        """
            No base URL provided. Either
              - export an environment variable $DEFAULT_VAR_NAME
              - set the global property `baseUrl`
        """.trimIndent()
    }
}
