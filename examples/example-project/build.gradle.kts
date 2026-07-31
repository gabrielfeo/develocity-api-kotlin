plugins {
    kotlin("jvm") version "2.4.10"
    application
}

application {
    mainClass = "com.gabrielfeo.develocity.api.example.MainKt"
}

dependencies {
    implementation("com.gabrielfeo:develocity-api-kotlin:2026.1.0")
}

repositories {
    mavenCentral()
}
