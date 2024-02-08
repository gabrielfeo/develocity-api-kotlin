plugins {
    id("com.gradle.enterprise") version("3.13.2")
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}

include(
    ":gradle-enterprise-api-kotlin",
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
