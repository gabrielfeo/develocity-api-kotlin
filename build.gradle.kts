plugins {
    val kotlinVersion = "1.7.10"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.dokka") version kotlinVersion
    id("org.openapi.generator") version "6.2.1"
    `java-library`
    `maven-publish`
}

group = "com.github.gabrielfeo"
version = "1.0"

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
    modelPackage.set("com.gabrielfeo.gradle.enterprise.api")
    packageName.set("com.gabrielfeo.gradle.enterprise.api")
    invokerPackage.set("com.gabrielfeo.gradle.enterprise.api")
    additionalProperties.put("library", "jvm-retrofit2")
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

tasks.named<Jar>("javadocJar") {
    from(tasks.dokkaJavadoc)
}

publishing {
    publications {
        create<MavenPublication>("library") {
            artifactId = "gradle-enterprise-api-kotlin"
            from(components["java"])
        }
    }
}

dependencies {
    api("com.squareup.moshi:moshi:1.13.0")
    api("com.squareup.moshi:moshi-kotlin:1.13.0")
    api("com.squareup.okhttp3:okhttp:4.10.0")
    api("com.squareup.okhttp3:logging-interceptor:4.10.0")
    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-moshi:2.9.0")
    api("com.squareup.retrofit2:converter-scalars:2.9.0")
}
