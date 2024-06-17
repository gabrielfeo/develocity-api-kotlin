pluginManagement {
    includeBuild("./build-logic")
}

plugins {
    id("com.gradle.develocity") version("3.17.5")
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
    }
}
