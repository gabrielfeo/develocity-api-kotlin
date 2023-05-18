plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

testing {
    suites {
        withType<JvmTestSuite> {
            useKotlinTest()
        }
    }
}

application {
    mainClass.set("com.gabrielfeo.gradle.enterprise.api.example.AppKt")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.gabrielfeo:gradle-enterprise-api-kotlin:0.15.1")
}
