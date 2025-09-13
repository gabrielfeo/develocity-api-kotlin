package com.gabrielfeo.develocity.api.example.notebook

import com.gabrielfeo.develocity.api.copyFromResources
import com.gabrielfeo.develocity.api.example.JsonAdapter
import com.gabrielfeo.develocity.api.example.Queries
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.div
import kotlin.io.path.writeText

@Execution(CONCURRENT)
class NotebooksTest {

    @Test
    fun testMostFrequentBuildsNotebook(@TempDir tempDir: Path) {
        val jupyter = setup(tempDir)
        val sourceNotebook = tempDir / "examples/example-notebooks/MostFrequentBuilds.ipynb"
        val fasterNotebook = jupyter.forceUseOfFasterQuery(sourceNotebook)
        val snapshotNotebook = jupyter.forceUseOfMavenLocalSnapshotArtifact(fasterNotebook)
        val executedNotebook = assertDoesNotThrow { jupyter.executeNotebook(snapshotNotebook) }
        with(JsonAdapter.fromJson(executedNotebook.outputNotebook).asNotebookJson()) {
            assertTrue(textOutputLines.any { Regex("""Collected \d+ builds from the API""").containsMatchIn(it) }) {
                "Expected line match not found in text outputs:\n${JsonAdapter.toPrettyJson(properties)}"
            }
            assertTrue(dataframeOutputs.isNotEmpty() && dataframeOutputs.none { it.isBlank() }) {
                "Expected Dataframe outputs not found in notebook:\n${JsonAdapter.toPrettyJson(properties)}"
            }
            assertTrue(kandyOutputs.isNotEmpty() && kandyOutputs.none { it.isEmpty() }) {
                "Expected Kandy outputs not found in notebook:\n${JsonAdapter.toPrettyJson(properties)}"
            }
        }
    }

    private fun setup(tempDir: Path): Jupyter {
        copyFromResources("/examples", tempDir)
        val venv = PythonVenv(tempDir / ".venv").also {
            it.installRequirements(
                requirementsFile = tempDir / "examples/example-notebooks/requirements.txt",
                linksDir = Path(System.getProperty("downloaded-requirements-path")).absolute(),
            )
        }
        return Jupyter(tempDir, venv.dir)
    }

    private fun Jupyter.forceUseOfFasterQuery(sourceNotebook: Path): Path = replacePattern(
        path = sourceNotebook,
        pattern = Regex("""query\s*=.+,"""),
        replacement = """query = "${Queries.FAST}",""",
    )

    @Test
    fun testLoggingNotebook(@TempDir tempDir: Path) {
        val jupyter = setup(tempDir)
        val sourceNotebook = tempDir / "examples/example-notebooks/Logging.ipynb"
        val snapshotNotebook = jupyter.forceUseOfMavenLocalSnapshotArtifact(sourceNotebook)
        val executedNotebook = assertDoesNotThrow { jupyter.executeNotebook(snapshotNotebook) }
        val kernelLogs = executedNotebook.outputStreams.stderr
        assertTrue(kernelLogs.contains("gabrielfeo.develocity.api.Cache - HTTP cache", ignoreCase = true))
    }

    private fun Jupyter.forceUseOfMavenLocalSnapshotArtifact(sourceNotebook: Path): Path {
        val mavenLocal = Path(System.getProperty("user.home"), ".m2/repository").toUri()
        val libraryDescriptor = (workDir / "develocity-api-kotlin.json").apply {
            writeText(buildLibraryDescriptor(version = "SNAPSHOT", repository = mavenLocal))
        }
        return replacePattern(
            path = sourceNotebook,
            pattern = Regex("(?:DependsOn|%use).*develocity-api-kotlin.*"),
            replacement = """
                %use develocity-api-kotlin@file[$libraryDescriptor]
                %trackClasspath on
                %logLevel debug
            """.trimIndent()
        )
    }

    @Suppress("SameParameterValue")
    private fun buildLibraryDescriptor(version: String, repository: URI) = """
        {
          "dependencies": ["com.gabrielfeo:develocity-api-kotlin:$version"],
          "repositories": ["$repository"]
        }
    """.trimIndent()
}
