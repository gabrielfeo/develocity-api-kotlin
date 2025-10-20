pluginManagement {
    includeBuild("./build-logic")
}

plugins {
    id("com.gradle.develocity") version("4.2.2")
    id("com.gradle.common-custom-user-data-gradle-plugin") version("2.4.0")
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

include(
    ":library",
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
        obfuscation {
            ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0" } }
            hostname { "-redacted-" }
        }
    }
}
