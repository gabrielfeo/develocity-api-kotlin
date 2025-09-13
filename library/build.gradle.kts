import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.gabrielfeo.published-kotlin-jvm-library")
    id("com.gabrielfeo.develocity-api-code-generation")
    id("com.gabrielfeo.integration-test-suite")
    id("com.gabrielfeo.examples-test-suite")
    id("com.gabrielfeo.test-fixtures")
}

// Order matters as this library is used as a Kotlin Jupyter kernel dependency (see #440)
dependencies {
    constraints {
        implementation(libs.okio)
    }
    // Set fixed version of stdlib for compatibility with the version of Kotlin
    // embedded in earlier versions of Gradle
    implementation(libs.kotlin.stdlib)
    api(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    api(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.retrofit.converter.scalars)
    api(libs.moshi)
    implementation(libs.moshi.kotlin)
    api(libs.kotlin.coroutines)
    implementation(libs.slf4j.api)
    compileOnly(libs.kotlin.jupyter.api)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.okio.fakeFileSystem)
    testImplementation(libs.okio)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.junit.jupiter.params)
    integrationTestImplementation(libs.kotlin.coroutines.test)
    integrationTestImplementation(libs.guava)
    integrationTestImplementation(libs.kotlin.jupyter.testkit)
    integrationTestImplementation(libs.logback.core)
    integrationTestImplementation(libs.logback.classic)
    integrationTestImplementation(libs.okhttp.mockwebserver)
}

val libraryPom = Action<MavenPom> {
    name = "Develocity API Kotlin"
    description = "A library to use the Develocity API in Kotlin"
    val repoUrl = providers.gradleProperty("repo.url")
    url = repoUrl
    licenses {
        license {
            name = "MIT"
            url = "https://spdx.org/licenses/MIT.html"
            distribution = "repo"
        }
    }
    developers {
        developer {
            id = "gabrielfeo"
            name = "Gabriel Feo"
            email = "gabriel@gabrielfeo.com"
        }
    }
    scm {
        val basicUrl = repoUrl.map { it.substringAfter("://") }
        connection = basicUrl.map { "scm:git:git://$it.git" }
        developerConnection = basicUrl.map { "scm:git:ssh://$it.git" }
        url = basicUrl.map { "https://$it/" }
    }
}

publishing {
    publications {
        register<MavenPublication>("develocityApiKotlin") {
            artifactId = "develocity-api-kotlin"
            from(components["java"])
            pom(libraryPom)
        }
        // For occasional maven local publishing
        register<MavenPublication>("unsignedDevelocityApiKotlin") {
            artifactId = "develocity-api-kotlin"
            from(components["java"])
            pom(libraryPom)
        }
        register<MavenPublication>("unsignedSnapshotDevelocityApiKotlin") {
            artifactId = "develocity-api-kotlin"
            version = "SNAPSHOT"
            from(components["java"])
            pom(libraryPom)
        }
        register<MavenPublication>("relocation") {
            artifactId = "gradle-enterprise-api-kotlin"
            pom {
                libraryPom(this)
                distributionManagement {
                    relocation {
                        groupId = project.group.toString()
                        artifactId = "develocity-api-kotlin"
                        message = "artifactId has been changed. Part of the rename to Develocity."
                    }
                }
            }
        }
    }
}

tasks.named("compileJava", JavaCompile::class) {
    sourceCompatibility = JavaVersion.VERSION_11.majorVersion
    targetCompatibility = JavaVersion.VERSION_11.majorVersion
}

tasks.named("compileKotlin", KotlinCompile::class) {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_1_8
        jvmTarget = JvmTarget.JVM_11
    }
}

tasks.withType<Test>().configureEach {
    maxParallelForks = 6
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "same_thread")
    val cleanupMode = System.getProperty("junit.jupiter.tempdir.cleanup.mode.default")
        ?: "always"
    systemProperty("junit.jupiter.tempdir.cleanup.mode.default", cleanupMode)
}

tasks.named<Test>("test") {
    environment = emptyMap()
}

tasks.named<Test>("integrationTest") {
    environment = emptyMap()
}

val publishUnsignedSnapshotDevelocityApiKotlinPublicationToMavenLocal by tasks.getting

tasks.named<Test>("examplesTest") {
    inputs.files(files(publishUnsignedSnapshotDevelocityApiKotlinPublicationToMavenLocal))
        .withPropertyName("snapshotPublicationArtifacts")
        .withNormalizer(ClasspathNormalizer::class)
    providers.environmentVariablesPrefixedBy("DEVELOCITY_API_").get().forEach { (name, value) ->
        inputs.property("${name}.hashCode", value.hashCode())
    }
}
