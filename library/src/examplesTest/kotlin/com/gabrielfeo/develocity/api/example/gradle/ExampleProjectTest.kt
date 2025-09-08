package com.gabrielfeo.develocity.api.example.gradle

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.div
import com.gabrielfeo.develocity.api.example.copyFromResources
import com.gabrielfeo.develocity.api.example.runInShell

@TestMethodOrder(OrderAnnotation::class)
class ExampleProjectTest {

    @TempDir
    lateinit var tempDir: Path

    private val projectDir
        get() = tempDir / "examples/example-project"

    private val initScriptPath
        get() = tempDir / ResourceInitScripts.FORCE_SNAPSHOT_LIBRARY

    @BeforeEach
    fun setup() {
        copyFromResources("/examples", tempDir)
        copyFromResources("/${ResourceInitScripts.FORCE_SNAPSHOT_LIBRARY}", tempDir)
    }

    @Test
    @Order(1)
    fun smokeTest() {
        val dependencies = runBuild("dependencies --configuration runtimeClasspath").stdout
        val libraryMatches = dependencies.lines().filter { "develocity-api-kotlin" in it }
        assertTrue(libraryMatches.isNotEmpty())
        assertTrue(libraryMatches.all { "-> SNAPSHOT" in it && "FAILED" !in it }) {
            "Expected forced SNAPSHOT versions, but found [${libraryMatches.joinToString(", ")}]"
        }
    }

    @Test
    fun testExampleProject() {
        val output = runBuild("run").stdout
        val tableRegex = Regex("""(?ms)^[-]+\nMost frequent builds:\n\s*\n(.+\|\s*\d+\s*\n?)+""")
        assertTrue(tableRegex.containsMatchIn(output)) {
            "Expected match for pattern '$tableRegex' in output '$output'"
        }
    }

    private fun runBuild(gradleArgs: String) =
        runInShell(
            projectDir,
            "./gradlew --stacktrace --no-daemon",
            "-I $initScriptPath",
            gradleArgs,
        )
}
