package com.gabrielfeo.gradle.enterprise.api

fun requireBaseUrl(
    varName: String = "GRADLE_ENTERPRISE_URL",
): String {
    return checkNotNull(System.getenv(varName))
}
