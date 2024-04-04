@file:Suppress("UnstableApiUsage")

import java.net.URL
import org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("com.gabrielfeo.kotlin-jvm-library")
    id("com.gabrielfeo.develocity-api-code-generation")
    id("com.gabrielfeo.test-suites")
    id("org.jetbrains.dokka")
    `java-library`
    `maven-publish`
    signing
}

val repoUrl = "https://github.com/gabrielfeo/gradle-enterprise-api-kotlin"

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        vendor.set(JvmVendorSpec.AZUL)
    }
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets.all {
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(URL("$repoUrl/blob/$version/src/main/kotlin"))
            remoteLineSuffix.set("#L")
        }
        jdkVersion.set(11)
        suppressGeneratedFiles.set(false)
        documentedVisibilities.set(setOf(PUBLIC))
        perPackageOption {
            matchingRegex.set(""".*\.internal.*""")
            suppress.set(true)
        }
        externalDocumentationLink("https://kotlinlang.org/api/kotlinx.coroutines/")
        externalDocumentationLink("https://square.github.io/okhttp/4.x/okhttp/")
        externalDocumentationLink("https://square.github.io/retrofit/2.x/retrofit/")
        externalDocumentationLink("https://square.github.io/moshi/1.x/moshi/")
        externalDocumentationLink("https://square.github.io/moshi/1.x/moshi-kotlin/")
    }
}

tasks.named<Jar>("javadocJar") {
    from(tasks.dokkaHtml)
}

tasks.named<Test>("integrationTest") {
    environment("GRADLE_ENTERPRISE_API_LOG_LEVEL", "DEBUG")
}

java {
    consistentResolution {
        useRuntimeClasspathVersions()
    }
}

dependencies {
    constraints {
        implementation("com.squareup.okio:okio:3.9.0")
    }
    api("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    api("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    api("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.slf4j:slf4j-api:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("com.squareup.okio:okio:3.9.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    integrationTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    integrationTestImplementation("com.google.guava:guava:33.1.0-jre")
}

publishing {
    publications {
        create<MavenPublication>("library") {
            artifactId = "gradle-enterprise-api-kotlin"
            from(components["java"])
            pom {
                name.set("Gradle Enterprise API Kotlin")
                description.set("A library to use the Gradle Enterprise REST API in Kotlin")
                url.set("https://github.com/gabrielfeo/gradle-enterprise-api-kotlin")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://spdx.org/licenses/MIT.html")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("gabrielfeo")
                        name.set("Gabriel Feo")
                        email.set("gabriel@gabrielfeo.com")
                    }
                }
                scm {
                    val basicUrl = "github.com/gabrielfeo/gradle-enterprise-api-kotlin"
                    connection.set("scm:git:git://$basicUrl.git")
                    developerConnection.set("scm:git:ssh://$basicUrl.git")
                    url.set("https://$basicUrl/")
                }
            }
        }
    }
    repositories {
        maven {
            name = "mavenCentral"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            val isSnapshot = version.toString().endsWith("SNAPSHOT")
            url = if (isSnapshot) snapshotsRepoUrl else releasesRepoUrl
            authentication {
                register<BasicAuthentication>("basic")
            }
            credentials {
                username = project.properties["maven.central.username"] as String?
                password = project.properties["maven.central.password"] as String?
            }
        }
    }
}

fun isCI() = System.getenv("CI").toBoolean()

signing {
    sign(publishing.publications["library"])
    if (isCI()) {
        useInMemoryPgpKeys(
            project.properties["signing.secretKey"] as String?,
            project.properties["signing.password"] as String?,
        )
    }
}
