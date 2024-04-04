package com.gabrielfeo

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
    val generateDir = project.layout.buildDirectory.file("generated-api")
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

tasks.openApiGenerate {
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
