package com.gabrielfeo.task

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.InputStream

class ForceNotebookToUseSnapshotTest {

    // Prefer over TempDir to inspect files after tests when troubleshooting
    private val tempDir: File = File("./build/test-workdir/")
    private val projectDir: File = tempDir
    private val inputPath = File(projectDir, "input.ipynb")
    private val outputPath = File(projectDir, "output.ipynb")

    @BeforeEach
    fun setup() {
        tempDir.deleteRecursively()
        tempDir.mkdirs()
        requireResourceAsStream("Simple.ipynb").copyTo(inputPath.outputStream())
        writeTestProject(inputPath, outputPath)
    }

    @Test
    fun replacesAllUseStatements() {
        runBuild(projectDir, listOf("forceNotebooksToUseSnapshot", "--stacktrace"))
        outputPath.readText().let {
            assertTrue("%use @${projectDir.absolutePath}/jupyter-maven-local-descriptor.json" in it)
            assertTrue("develocity-api-kotlin" !in it)
        }
    }

    private fun requireResourceAsStream(path: String): InputStream =
        checkNotNull(this::class.java.classLoader.getResourceAsStream(path)) {
            "Required resource $path does not exist"
        }

    @Suppress("SameParameterValue")
    private fun runBuild(projectDir: File, args: List<String>) {
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(args)
            .forwardOutput()
            .build()
    }

    private fun writeTestProject(inputPath: File, outputPath: File): File {
        File(projectDir, "settings.gradle").writeText("")
        File(projectDir, "build.gradle").writeText(
            // language=groovy
            """
                import com.gabrielfeo.task.ForceNotebooksToUseSnapshot

                plugins {
                    id("com.gabrielfeo.no-op")
                }

                tasks.register("forceNotebooksToUseSnapshot", ForceNotebooksToUseSnapshot) {
                    version.set("2024.1.1")
                    originalNotebook.set(file("${inputPath.relativeTo(projectDir)}"))
                    modifiedNotebook.set(file("${outputPath.relativeTo(projectDir)}"))
                    
                }
            """.trimIndent()
        )
        return projectDir
    }
}
