@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.AZUL
    }
}

testing {
    suites {
        register<JvmTestSuite>("functionalTest") {
            useJUnitJupiter()
        }
    }
}

gradlePlugin {
    testSourceSets(sourceSets["functionalTest"])
}

tasks.named("check") {
    dependsOn(tasks.withType<Test>())
}

dependencies {
    implementation(libs.kotlin.plugin)
    implementation(libs.kotlin.binary.compatibility.validator.plugin)
    implementation(libs.dokka.plugin)
    implementation(libs.openapi.generator.plugin)
    implementation(libs.vanniktech.mavenPublishPlugin)
    "functionalTestImplementation"(project)
}
