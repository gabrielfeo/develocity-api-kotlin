@file:Suppress("UnstableApiUsage")

import java.net.URL
import org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("com.gabrielfeo.kotlin-jvm-library")
    id("com.gabrielfeo.test-suites")
    id("org.jetbrains.dokka")
    id("org.openapi.generator")
    `java-library`
    `maven-publish`
    signing
}

val repoUrl = "https://github.com/gabrielfeo/gradle-enterprise-api-kotlin"

val localSpecPath = providers.gradleProperty("localSpecPath")
val remoteSpecUrl = providers.gradleProperty("remoteSpecUrl").orElse(
    providers.gradleProperty("gradle.enterprise.version").map { geVersion ->
        val specName = "gradle-enterprise-$geVersion-api.yaml"
        "https://docs.gradle.com/enterprise/api-manual/ref/$specName"
    }
)

val downloadApiSpec by tasks.registering {
    onlyIf { !localSpecPath.isPresent() }
    val spec = resources.text.fromUri(remoteSpecUrl)
    val specName = remoteSpecUrl.map { it.substringAfterLast('/') }
    val outFile = project.layout.buildDirectory.file(specName)
    inputs.property("Spec URL", remoteSpecUrl)
    outputs.file(outFile)
    doLast {
        logger.info("Downloaded API spec from ${remoteSpecUrl.get()}")
        spec.asFile().renameTo(outFile.get().asFile)
    }
}

openApiGenerate {
    generatorName.set("kotlin")
    val spec = when {
        localSpecPath.isPresent() -> localSpecPath.map { rootProject.file(it).absolutePath }
        else -> downloadApiSpec.map { it.outputs.files.first().absolutePath }
    }
    inputSpec.set(spec)
    val generateDir = project.layout.buildDirectory.file("generated/openapi-generator")
    outputDir.set(generateDir.map { it.asFile.absolutePath })
    val ignoreFile = project.layout.projectDirectory.file(".openapi-generator-ignore")
    ignoreFileOverride.set(ignoreFile.asFile.absolutePath)
    apiPackage.set("com.gabrielfeo.gradle.enterprise.api")
    modelPackage.set("com.gabrielfeo.gradle.enterprise.api.model")
    packageName.set("com.gabrielfeo.gradle.enterprise.api.internal")
    invokerPackage.set("com.gabrielfeo.gradle.enterprise.api.internal")
    additionalProperties.put("library", "jvm-retrofit2")
    additionalProperties.put("useCoroutines", true)
}

tasks.openApiGenerate.configure {
    val srcDir = File(outputDir.get(), "src/main/kotlin")
    doFirst {
        logger.info("Using API spec ${inputSpec.get()}")
    }
    // Replace Response<X> with X in every method return type of GradleEnterpriseApi.kt
    doLast {
        ant.withGroovyBuilder {
            "replaceregexp"(
                "match" to ": Response<(.*?)>$",
                "replace" to """: \1""",
                "flags" to "gm",
            ) {
                "fileset"(
                    "dir" to srcDir,
                    "includes" to "com/gabrielfeo/gradle/enterprise/api/*Api.kt",
                )
            }
        }
    }
    // Add @JvmSuppressWildcards to avoid square/retrofit#3275
    doLast {
        ant.withGroovyBuilder {
            "replaceregexp"(
                "match" to "interface",
                "replace" to """
                    @JvmSuppressWildcards
                    interface
                """.trimIndent(),
                "flags" to "m",
            ) {
                "fileset"(
                    "dir" to srcDir,
                    "includes" to "com/gabrielfeo/gradle/enterprise/api/*Api.kt",
                )
            }
        }
    }
    // Workaround for properties generated with `arrayListOf(null,null)` as default value
    doLast {
        ant.withGroovyBuilder {
            "replaceregexp"(
                "match" to """arrayListOf\(null,null\)""",
                "replace" to """emptyList()""",
                "flags" to "gm",
            ) {
                "fileset"(
                    "dir" to srcDir
                )
            }
        }
    }
    // Workaround for missing imports of exploded queries
    doLast {
        val modelPackage = openApiGenerate.modelPackage.get()
        val modelPackagePattern = modelPackage.replace(".", "\\.")
        ant.withGroovyBuilder {
            "replaceregexp"(
                "match" to """(?:import $modelPackagePattern.[.\w]+\s)+""",
                "replace" to "import $modelPackage.*\n",
                "flags" to "m",
            ) {
                "fileset"(
                    "dir" to srcDir
                )
            }
        }
    }
    // Fix mapping of BuildModelName: gradle-attributes -> gradleAttributes
    doLast {
        ant.withGroovyBuilder {
            "replaceregexp"(
                "match" to "Minus",
                "replace" to "",
                "flags" to "mg",
            ) {
                "fileset"(
                    "dir" to srcDir,
                    "includes" to "com/gabrielfeo/gradle/enterprise/api/model/BuildModelName.kt",
                )
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDir(tasks.openApiGenerate)
        }
    }
}

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
    jvmArgs("-Xmx512m")
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
