pluginManagement {
    includeBuild("./build-logic")
}

plugins {
    id("com.gradle.enterprise") version("3.13.2")
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}

include(
    ":library",
    ":examples:example-project:app",
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
