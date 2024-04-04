package com.gabrielfeo

import com.gabrielfeo.task.PostProcessGeneratedApi
import org.gradle.kotlin.dsl.*

plugins {
    id("com.gabrielfeo.kotlin-jvm-library")
    id("org.openapi.generator")
}

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
    val generateDir = project.layout.buildDirectory.dir("generated-api")
        .map { it.asFile.absolutePath }
    outputDir.set(generateDir)
    val ignoreFile = project.layout.projectDirectory.file(".openapi-generator-ignore")
    ignoreFileOverride.set(ignoreFile.asFile.absolutePath)
    apiPackage.set("com.gabrielfeo.gradle.enterprise.api")
    modelPackage.set("com.gabrielfeo.gradle.enterprise.api.model")
    packageName.set("com.gabrielfeo.gradle.enterprise.api.internal")
    invokerPackage.set("com.gabrielfeo.gradle.enterprise.api.internal")
    additionalProperties.put("library", "jvm-retrofit2")
    additionalProperties.put("useCoroutines", true)
}

val postProcessGeneratedApi by tasks.registering(PostProcessGeneratedApi::class) {
    val generatedSrc = tasks.openApiGenerate
        .flatMap { it.outputDir }
        .map { File(it, "src/main/kotlin") }
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
