rootProject.name = "example-project"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}

include(":app")
