plugins {
    kotlin("jvm") version "2.2.10"
    application
}

application {
    mainClass = "com.gabrielfeo.develocity.api.example.MainKt"
}

dependencies {
    implementation("com.gabrielfeo:develocity-api-kotlin:2024.3.0")
}

repositories {
    mavenCentral()
}
