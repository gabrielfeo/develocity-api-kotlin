package com.gabrielfeo.develocity.api.example.gradle

import com.gabrielfeo.develocity.api.example.BuildStartTime
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import com.gabrielfeo.develocity.api.example.copyFromResources
import com.gabrielfeo.develocity.api.example.runInShell
import kotlin.io.path.div
import java.nio.file.Files

@TestMethodOrder(OrderAnnotation::class)
class ExampleGradleTaskTest {

    @TempDir
    lateinit var tempDir: Path

    private val projectDir
        get() = tempDir / "examples/example-gradle-task"

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
        val dependencies = runBuild(":buildSrc:dependencies --configuration runtimeClasspath").stdout
        val libraryMatches = dependencies.lines().filter { "develocity-api-kotlin" in it }
        assertTrue(libraryMatches.isNotEmpty())
        assertTrue(libraryMatches.all { "-> SNAPSHOT" in it && "FAILED" !in it }) {
            "Expected forced SNAPSHOT versions, but found [${libraryMatches.joinToString(", ")}]"
        }
    }

    @Test
    fun testBuildPerformanceMetricsTask() {
        val args = "--user runner --period=${BuildStartTime.RECENT}"
        val output = runBuild("userBuildPerformanceMetrics $args").stdout
        assertPerformanceMetricsOutput(output, user = "runner", period = BuildStartTime.RECENT)
    }

    private fun runBuild(gradleArgs: String) =
        runInShell(
            projectDir,
            "./gradlew --stacktrace --no-daemon",
            "-I $initScriptPath",
            gradleArgs,
        )

    private fun assertPerformanceMetricsOutput(
        output: String,
        user: String,
        period: String,
    ) {
        val expectedHeading = "Build performance overview for $user since $period (powered by Develocity®)"
        assertTrue(output.contains(expectedHeading))
        assertTrue(output.contains("▶︎ Serialization factor:"))
        assertTrue(output.contains("⏩︎ Avoidance savings:"))
    }
}
