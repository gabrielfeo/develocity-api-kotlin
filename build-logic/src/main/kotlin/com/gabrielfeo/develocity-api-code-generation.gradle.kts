package com.gabrielfeo

import com.gabrielfeo.task.PostProcessGeneratedApi
import org.gradle.kotlin.dsl.*

plugins {
    id("com.gabrielfeo.kotlin-jvm-library")
    id("org.openapi.generator")
}

val localSpecPath = providers.gradleProperty("localSpecPath")
val remoteSpecUrl = providers.gradleProperty("remoteSpecUrl").orElse(
    providers.gradleProperty("develocity.version").map { geVersion ->
        val majorVersion = geVersion.substringBefore('.').toInt()
        val specName = when {
            majorVersion <= 2023 -> "gradle-enterprise-$geVersion-api.yaml"
            else -> "develocity-$geVersion-api.yaml"
        }
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
    generatorName = "kotlin"
    val spec = when {
        localSpecPath.isPresent() -> localSpecPath.map { rootProject.file(it).absolutePath }
        else -> downloadApiSpec.map { it.outputs.files.first().absolutePath }
    }
    inputSpec = spec
    val generateDir = project.layout.buildDirectory.dir("generated-api")
        .map { it.asFile.absolutePath }
    outputDir = generateDir
    val ignoreFile = project.layout.projectDirectory.file(".openapi-generator-ignore")
    ignoreFileOverride = ignoreFile.asFile.absolutePath
    apiPackage = "com.gabrielfeo.develocity.api"
    modelPackage = "com.gabrielfeo.develocity.api.model"
    packageName = "com.gabrielfeo.develocity.api.internal"
    invokerPackage = "com.gabrielfeo.develocity.api.internal"
    additionalProperties.put("library", "jvm-retrofit2")
    additionalProperties.put("useCoroutines", true)
    additionalProperties.put("enumPropertyNaming", "camelCase")
    cleanupOutput = true
}

val postProcessGeneratedApi by tasks.registering(PostProcessGeneratedApi::class) {
    val generatedSrc = tasks.openApiGenerate
        .flatMap { it.outputDir }
        .map { File(it) }
    originalFiles.convention(project.layout.dir(generatedSrc))
    postProcessedFiles.convention(project.layout.buildDirectory.dir("post-processed-api"))
    modelsPackage.convention(tasks.openApiGenerate.flatMap { it.modelPackage })
}

sourceSets {
    main {
        java {
            srcDir(postProcessGeneratedApi)
        }
    }
}
