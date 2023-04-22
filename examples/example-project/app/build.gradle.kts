plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    application
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest()
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

application {
    mainClass.set("com.gabrielfeo.gradle.enterprise.api.example.AppKt")
}
