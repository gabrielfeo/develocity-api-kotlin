plugins {
    // in your project, replace for id("org.jetbrains.kotlin.jvm")
    id("com.gabrielfeo.kotlin-jvm-library")
    application
}

application {
    mainClass.set("com.gabrielfeo.develocity.api.example.MainKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation("com.gabrielfeo:develocity-api-kotlin:2024.2.0-alpha01")
}
