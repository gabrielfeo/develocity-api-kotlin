plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

application {
    mainClass.set("com.gabrielfeo.gradle.enterprise.api.example.MainKt")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.gabrielfeo:gradle-enterprise-api-kotlin:0.16.0")
}
