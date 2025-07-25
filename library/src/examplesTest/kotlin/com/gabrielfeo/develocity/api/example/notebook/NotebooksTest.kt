package com.gabrielfeo.develocity.api.example.notebook

import com.gabrielfeo.develocity.api.example.JsonAdapter
import com.gabrielfeo.develocity.api.example.copyFromResources
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.div

class NotebooksTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    lateinit var tempDir: Path

    lateinit var venv: PythonVenv

    lateinit var jupyter: Jupyter

    @BeforeEach
    fun setup() {
        copyFromResources("/examples", tempDir)
        venv = PythonVenv(tempDir / ".venv").also {
            it.installRequirements(
                requirementsFile = tempDir / "examples/example-notebooks/requirements.txt",
                linksDir = Path(System.getProperty("downloaded-requirements-path")).absolute(),
            )
        }
        jupyter = Jupyter(tempDir, venv.dir)
    }

    @Test
    fun testMostFrequentBuildsNotebook() {
        val sourceNotebook = tempDir / "examples/example-notebooks/MostFrequentBuilds.ipynb"
        val snapshotNotebook = forceUseOfMavenLocalSnapshotArtifact(sourceNotebook)
        val executedNotebook = assertDoesNotThrow { jupyter.executeNotebook(snapshotNotebook) }
        with(JsonAdapter.fromJson(executedNotebook).asNotebookJson()) {
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

    private fun forceUseOfMavenLocalSnapshotArtifact(sourceNotebook: Path): Path {
        val mavenLocal = Path(System.getProperty("user.home"), ".m2/repository").toUri()
        return jupyter.replaceMagics(
            path = sourceNotebook,
            replacePattern = Regex("""(?:DependsOn|%use).*develocity-api-kotlin.*"""),
            replacement = listOf(
                """@file:DependsOn("com.gabrielfeo:develocity-api-kotlin:SNAPSHOT")""",
                """@file:Repository("$mavenLocal")""",
                """%trackClasspath on""",
                """import com.gabrielfeo.develocity.api.*""",
                """import com.gabrielfeo.develocity.api.model.*""",
                """import com.gabrielfeo.develocity.api.extension.*""",
            ).joinToString("\n")
        )
    }


}