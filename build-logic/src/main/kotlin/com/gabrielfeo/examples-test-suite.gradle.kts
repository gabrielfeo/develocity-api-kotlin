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

val enforceSameQueries by tasks.registering {
    val allowedQueries = setOf(
        """buildStartTime>-7d buildTool:gradle""",
        """user:"${'$'}user" buildStartTime>${'$'}startTime"""
    )
    inputs.property("allowedQueries", allowedQueries)
    inputs.files(examples)
        .withPropertyName("exampleFiles")
        .withPathSensitivity(PathSensitivity.NONE)
        .skipWhenEmpty()
    outputs.cacheIf { true }
    doLast {
        for (file in examples.files) {
            file.useLines { lines ->
                val query = lines.firstOrNull { Regex("""query\s*=""").containsMatchIn(it) }
                    ?.trim('\n', ' ', ',', '=', '"', '\'', '\\')
                    ?.substringAfter("query")
                    ?.trim('\n', ' ', ',', '=', '"', '\'', '\\')
                    ?: continue
                if (query !in allowedQueries) {
                    throw VerificationException("Query not in allowedQueries: '$query' ($file). Allowed: $allowedQueries")
                }
            }
        }
    }
}
