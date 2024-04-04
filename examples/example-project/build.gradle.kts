plugins {
    id("org.jetbrains.kotlin.jvm")
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
    implementation("com.gabrielfeo:gradle-enterprise-api-kotlin:2023.4")
}
