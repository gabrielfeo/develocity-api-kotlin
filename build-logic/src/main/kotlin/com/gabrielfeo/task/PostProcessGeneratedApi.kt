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
        ant.withGroovyBuilder {
            "replaceregexp"(
                "match" to ": Response<(.*?)>$",
                "replace" to """: \1""",
                "flags" to "gm",
            ) {
                "fileset"(
                    "dir" to srcDir,
                    "includes" to "com/gabrielfeo/develocity/api/*Api.kt",
                )
            }
        }
        // Add @JvmSuppressWildcards to avoid square/retrofit#3275
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
                    "includes" to "com/gabrielfeo/develocity/api/*Api.kt",
                )
            }
        }
        // Fix mapping of BuildModelName: gradle-attributes -> gradleAttributes
        ant.withGroovyBuilder {
            "replaceregexp"(
                "match" to "Minus",
                "replace" to "",
                "flags" to "mg",
            ) {
                "fileset"(
                    "dir" to srcDir,
                    "includes" to "com/gabrielfeo/develocity/api/model/BuildModelName.kt",
                )
            }
        }
    }
}
