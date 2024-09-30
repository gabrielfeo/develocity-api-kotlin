pluginManagement {
    includeBuild("./build-logic")
}

plugins {
    id("com.gradle.develocity") version("3.18")
    id("com.gradle.common-custom-user-data-gradle-plugin") version("2.0.2")
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

include(
    ":library",
    ":examples",
    ":examples:example-project",
)

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        capture {
            buildLogging = false
            testLogging = false
        }
        obfuscation {
            ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0" } }
            hostname { "-redacted-" }
        }
    }
}
