plugins {
    id("com.gabrielfeo.kotlin-jvm-library")
    id("com.gabrielfeo.develocity-api-code-generation")
    id("com.gabrielfeo.test-suites")
    `java-library`
    id("com.gabrielfeo.library-publishing")
    alias(libs.plugins.kotlin.jupyter)
}

group = properties["library.group"]
artifactId = properties["library.artifact"]
version = properties["library.version"]

tasks.processJupyterApiResources {
    libraryProducers = listOf(
        "com.gabrielfeo.develocity.api.internal.jupyter.DevelocityApiJupyterIntegration",
    )
}

tasks.named<Test>("integrationTest") {
    environment("DEVELOCITY_API_LOG_LEVEL", "DEBUG")
}

java {
    consistentResolution {
        useRuntimeClasspathVersions()
    }
}

dependencies {
    constraints {
        implementation(libs.okio)
    }
    api(libs.moshi)
    implementation(libs.moshi.kotlin)
    api(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    api(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.retrofit.converter.scalars)
    api(libs.kotlin.coroutines)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.slf4j.simple)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.okio)
    testImplementation(libs.kotlin.coroutines.test)
    integrationTestImplementation(libs.kotlin.coroutines.test)
    integrationTestImplementation(libs.guava)
    integrationTestImplementation(libs.kotlin.jupyter.testkit)
}
