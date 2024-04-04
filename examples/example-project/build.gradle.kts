plugins {
    // in your project, replace for id("org.jetbrains.kotlin.jvm")
    id("com.gabrielfeo.kotlin-jvm-library")
    application
}

application {
    mainClass.set("com.gabrielfeo.gradle.enterprise.api.example.MainKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation("com.gabrielfeo:gradle-enterprise-api-kotlin:2023.4.0")
}
