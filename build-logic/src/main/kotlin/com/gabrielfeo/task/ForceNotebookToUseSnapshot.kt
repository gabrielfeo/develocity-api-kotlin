package com.gabrielfeo.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import javax.inject.Inject

abstract class ForceNotebooksToUseSnapshot @Inject constructor(
    private val fs: FileSystemOperations,
) : DefaultTask() {

    @get:InputFile
    val originalNotebook = project.objects.fileProperty()

    @get:Input
    abstract val version: Property<String>

    @get:OutputFile
    val modifiedNotebook = project.objects.fileProperty()

    @TaskAction
    fun doWork() {
        val modifiedDir = modifiedNotebook.asFile.get().parentFile
        val newDescriptor = writeMavenLocalDescriptor(dir = modifiedDir)
        fs.copy {
            from(originalNotebook)
            into(modifiedDir)
            rename { "output.ipynb" }
            filter { l ->
                if ("%use develocity-api-kotlin" in l) l
                    .replace("%use develocity-api-kotlin", "%use @$newDescriptor")
                    .replace("(version=2023.4.0)", "")
                else l
            }
        }
    }

    private fun writeMavenLocalDescriptor(dir: File): File {
        val newDescriptor = dir.resolve("jupyter-maven-local-descriptor.json")
        val content = DESCRIPTOR.replace("{{VERSION}}", version.get())
            .replace("{{HOME}}", System.getProperty("user.home"))
        newDescriptor.writeText(content)
        return newDescriptor
    }

}

// language=json
const val DESCRIPTOR = """
{
  "repositories": [
    "{{HOME}}/.m2/repository"
  ],
  "dependencies": [
    "com.gabrielfeo:develocity-api-kotlin:{{VERSION}}"
  ],
  "imports": [
    "com.gabrielfeo.develocity.api.*",
    "com.gabrielfeo.develocity.api.model.*",
    "com.gabrielfeo.develocity.api.extension.*"
  ]
}
"""
