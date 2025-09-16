package com.gabrielfeo

import com.gabrielfeo.task.PostProcessGeneratedApi
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("com.gabrielfeo.kotlin-jvm-library")
    id("org.openapi.generator")
}

val downloadApiSpec by tasks.registering {
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

fun openApiGenerateTask(
    outputDir: Provider<Directory>,
    ignoreFile: RegularFile,
    templateDir: Directory? = null,
) = tasks.registering(GenerateTask::class) {
    generatorName = "kotlin"
    inputSpec = downloadApiSpec.map { it.outputs.files.first().absolutePath }
    this.outputDir = outputDir.map { it.asFile.absolutePath }
    ignoreFileOverride.set(ignoreFile.asFile.absolutePath)
    this.templateDir.set(templateDir?.asFile?.absolutePath)
    apiPackage = "com.gabrielfeo.develocity.api"
    modelPackage = "com.gabrielfeo.develocity.api.model"
    packageName = "com.gabrielfeo.develocity.api.internal"
    invokerPackage = "com.gabrielfeo.develocity.api.internal"
    additionalProperties.put("library", "jvm-retrofit2")
    additionalProperties.put("useCoroutines", true)
    additionalProperties.put("enumPropertyNaming", "camelCase")
    cleanupOutput = true
}

val generateDevelocityApiMain by openApiGenerateTask(
    outputDir = project.layout.buildDirectory.dir("generated-api-main"),
    ignoreFile = project.layout.projectDirectory.file("src/main/.openapi-generator-ignore"),
)

fun postProcessingTask(
    generateTask: Provider<GenerateTask>,
    outputDir: Provider<Directory>,
) = tasks.registering(PostProcessGeneratedApi::class) {
        val generatedSrc = generateTask.flatMap { it.outputDir }.map { File(it) }
        originalFiles.convention(project.layout.dir(generatedSrc))
        postProcessedFiles.convention(outputDir)
        modelsPackage.convention(generateTask.flatMap { it.modelPackage })
    }

val postProcessGeneratedApiMain by postProcessingTask(
    generateTask = generateDevelocityApiMain,
    outputDir = project.layout.buildDirectory.dir("post-processed-api-main"),
)

sourceSets {
    main {
        java {
            srcDir(postProcessGeneratedApiMain)
        }
    }
}

plugins.withType<TestFixturesPlugin>().whenObjectAdded {
    val generateDevelocityApiTestFixtures by openApiGenerateTask(
        outputDir = project.layout.buildDirectory.dir("generated-api-test-fixtures"),
        // TODO Other param fixtures need to be ported over. Start with original templates. Core issue is that default values cannot be re-declared in the fake, but try leaving all annotations out for simplicity, as in headerParams.mustache.
        // cp ~/p/gradle/openapi-generator/modules/openapi-generator/src/main/resources/kotlin-client/libraries/jvm-retrofit2/*Param* library/src/testFixtures/test-fixture-templates/
        templateDir = project.layout.projectDirectory.dir("src/testFixtures/test-fixture-templates"),
        ignoreFile = project.layout.projectDirectory.file("src/testFixtures/.openapi-generator-ignore"),
    )
    val postProcessGeneratedApiTestFixtures by postProcessingTask(
        generateTask = generateDevelocityApiTestFixtures,
        outputDir = project.layout.buildDirectory.dir("post-processed-api-test-fixtures"),
    )
    sourceSets {
        named("testFixtures") {
            java {
                srcDir(postProcessGeneratedApiTestFixtures)
            }
        }
    }
}
