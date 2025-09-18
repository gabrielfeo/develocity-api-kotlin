package com.gabrielfeo.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.withGroovyBuilder
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class PostProcessGeneratedApi @Inject constructor(
    private val fsOperations: FileSystemOperations,
) : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val originalFiles: DirectoryProperty

    @get:Input
    abstract val modelsPackage: Property<String>

    @get:OutputDirectory
    abstract val postProcessedFiles: DirectoryProperty

    @TaskAction
    fun doWork() {
        postProcessedFiles.get().asFile.deleteRecursively()
        fsOperations.copy {
            from(originalFiles)
            into(postProcessedFiles)
        }
        postProcess(
            srcDir = postProcessedFiles.get().dir("src/main/kotlin").asFile,
            modelsPackage = modelsPackage.get()
        )
    }

    private fun postProcess(srcDir: File, modelsPackage: String) {
        // Replace Response<X> with X in every method return type of DevelocityApi.kt
        replaceAll(
            match = ": Response<(.*?)>$",
            replace = """: \1""",
            dir = srcDir,
            includes = "com/gabrielfeo/develocity/api/*Api.kt",
        )
        // Add @JvmSuppressWildcards to avoid square/retrofit#3275
        replaceAll(
            match = "interface",
            replace = "@JvmSuppressWildcards\ninterface",
            dir = srcDir,
            includes = "com/gabrielfeo/develocity/api/*Api.kt",
        )
        // Fix mapping of BuildModelName: gradle-attributes -> gradleAttributes
        replaceAll(
            match = "Minus",
            replace = "",
            dir = srcDir,
            includes = "com/gabrielfeo/develocity/api/model/BuildModelName.kt",
        )
        // Fix mapping of GradleConfigurationCacheResult.Outcome: hIT -> hit
        val file = "com/gabrielfeo/develocity/api/model/GradleConfigurationCacheResult.kt"
        replaceAll("hIT", "hit", dir = srcDir, includes = file)
        replaceAll("mISS", "miss", dir = srcDir, includes = file)
        replaceAll("fAILED", "failed", dir = srcDir, includes = file)

        // Fix mapping of MavenExtension.Type: lIBEXT -> core
        val mavenExtensionFile = "com/gabrielfeo/develocity/api/model/MavenExtension.kt"
        mapOf(
            "cORE" to "core",
            "mAVENEXTCLASSPATH" to "mavenExtClasspath",
            "lIBEXT" to "libExt",
            "pROJECT" to "project",
            "pOM" to "pom",
            "uNKNOWN" to "unknown",
        ).forEach { (match, replace) ->
            replaceAll(match, replace, dir = srcDir, includes = mavenExtensionFile)
        }
    }

    private fun replaceAll(
        match: String,
        replace: String,
        dir: File,
        includes: String,
    ) {
        ant.withGroovyBuilder {
            "replaceregexp"(
                "match" to match,
                "replace" to replace,
                "flags" to "mg",
            ) {
                "fileset"(
                    "dir" to dir,
                    "includes" to includes,
                )
            }
        }
    }
}
