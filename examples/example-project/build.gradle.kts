plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

application {
    mainClass = "com.gabrielfeo.develocity.api.example.MainKt"
}

dependencies {
    implementation("com.gabrielfeo:develocity-api-kotlin:2024.2.0-alpha02")
}
