@file:Suppress("UnstableApiUsage")

package com.gabrielfeo

plugins {
    id("org.jetbrains.kotlin.jvm")
}

testing {
    suites {
        register<JvmTestSuite>("examplesTest") {
            useKotlinTest()
        }
    }
}

kotlin {
    target {
        val main by compilations.getting
        val examplesTest by compilations.getting
        examplesTest.associateWith(main)
    }
}

val examples = fileTree(rootDir) {
    include("examples/**")
    exclude {
        it.isDirectory
            && (it.name == "build" || it.name.startsWith("."))
            && !it.path.endsWith("buildSrc/src/main/kotlin/build")
    }
}

tasks.named("processExamplesTestResources", ProcessResources::class) {
    from(examples)
}

val downloadPipRequirements by tasks.registering(Exec::class) {
    val requirementsFiles = examples.filter { it.name == "requirements.txt" }
    inputs.files(requirementsFiles)
        .withPropertyName("requirementsFiles")
        .withPathSensitivity(PathSensitivity.NONE)
        .skipWhenEmpty()
    val downloadDir = layout.buildDirectory.dir("pip-requirements")
    outputs.dir(downloadDir)
    commandLine("pip3", "download")
    workingDir(downloadDir)
    argumentProviders += CommandLineArgumentProvider {
        requirementsFiles.files.flatMap { listOf("-r", it.absolutePath) }
    }
}

tasks.named<Test>("examplesTest") {
    inputs.files(downloadPipRequirements)
        .withPropertyName("downloadedPipRequirements")
        .withPathSensitivity(PathSensitivity.NONE)
    systemProperty(
        "downloaded-requirements-path",
        downloadPipRequirements.map { it.outputs.files.singleFile }.get().relativeTo(workingDir).path,
    )
}

tasks.named("check") {
    dependsOn("examplesTest")
}
