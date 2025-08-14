pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("com.gradle.develocity") version("4.1")
    id("com.gradle.common-custom-user-data-gradle-plugin") version("2.3")
    id("build.logic.performance-metrics-plugin")
}

rootProject.name = "dak-example-build-logic-main-build"

develocity {
    buildScan {
        server = "https://ge.solutions-team.gradle.com"
        obfuscation {
            ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0" } }
            hostname { "-redacted-" }
        }
    }
}

println("Hello from example-build-logic settings.gradle.kts!")
