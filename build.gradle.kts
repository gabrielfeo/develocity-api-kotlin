import java.net.URL
import org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.jetbrains.dokka") version "1.7.20"
    id("org.openapi.generator") version "6.2.1"
    `java-library`
    `maven-publish`
}

group = "com.github.gabrielfeo"
version = "SNAPSHOT"
val repoUrl = "https://github.com/gabrielfeo/gradle-enterprise-api-kotlin"

val downloadApiSpec by tasks.registering {
    val geVersion = providers.gradleProperty("gradle.enterprise.version").get()
    val specName = "gradle-enterprise-$geVersion-api.yaml"
    val spec = resources.text.fromUri("https://docs.gradle.com/enterprise/api-manual/ref/$specName")
    val outFile = project.layout.buildDirectory.file(specName)
    inputs.property("GE version", geVersion)
    outputs.file(outFile)
    doLast {
        spec.asFile().renameTo(outFile.get().asFile)
    }
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set(downloadApiSpec.map { it.outputs.files.first().absolutePath })
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
    // Replace Response<X> with X in every method return type of GradleEnterpriseApi.kt
    doLast {
        val apiFile = File(
            outputDir.get(),
            "src/main/kotlin/com/gabrielfeo/gradle/enterprise/api/GradleEnterpriseApi.kt",
        )
        ant.withGroovyBuilder {
            "replaceregexp"(
                "file" to apiFile,
                "match" to ": Response<(.*?)>$",
                "replace" to """: \1""",
                "flags" to "gm",
            )
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
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets.all {
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(URL("$repoUrl/blob/$version/src/main/kotlin"))
            remoteLineSuffix.set("#L")
        }
        jdkVersion.set(8)
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

publishing {
    publications {
        create<MavenPublication>("library") {
            artifactId = "gradle-enterprise-api-kotlin"
            from(components["java"])
        }
    }
}

testing {
    suites.withType<JvmTestSuite> {
        useKotlinTest()
    }
}

dependencies {
    api("com.squareup.moshi:moshi:1.14.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    api("com.squareup.okhttp3:okhttp:4.10.0")
    api("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}
