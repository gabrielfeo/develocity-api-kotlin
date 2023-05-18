plugins {
    id("com.gradle.enterprise") version("3.13.2")
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
        value("Gradle: StartParameter", gradle.startParameter.toString())
    }
}
