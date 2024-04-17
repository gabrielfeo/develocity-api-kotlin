pluginManagement {
    includeBuild("./build-logic")
}

plugins {
    id("com.gradle.enterprise") version("3.17.2")
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

gradleEnterprise {
    buildScan {
        publishAlways()
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
