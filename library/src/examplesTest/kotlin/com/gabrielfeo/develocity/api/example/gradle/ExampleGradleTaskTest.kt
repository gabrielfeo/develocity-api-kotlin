package com.gabrielfeo.develocity.api.example.gradle

import org.junit.jupiter.api.Assertions.assertFalse
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

@TestMethodOrder(OrderAnnotation::class)
class ExampleGradleTaskTest {

    @TempDir
    lateinit var tempDir: Path

    private val projectDir
        get() = tempDir / "examples/example-gradle-task"

    @BeforeEach
    fun setup() {
        copyFromResources("/examples", tempDir)
        copyFromResources("/${ResourceInitScripts.FORCE_SNAPSHOT_LIBRARY}", tempDir)
        copyFromResources("/${ResourceInitScripts.REQUIRE_JAVA_11_COMPATIBILITY}", tempDir)
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
    fun testBuildPerformanceMetricsTaskWithDefaults() {
        val user = System.getProperty("user.name")
        val output = runBuild("userBuildPerformanceMetrics").stdout
        assertPerformanceMetricsOutput(output, user = user, period = "-14d")
    }

    @Test
    fun testJavaVersionCompatibility() {
        val initScript = tempDir / ResourceInitScripts.REQUIRE_JAVA_11_COMPATIBILITY
        val output = runBuild("-p buildSrc :generateExternalPluginSpecBuilders -I '$initScript'").stdout
        assertFalse(Regex("""FAILED|Could not resolve|No matching variant""").containsMatchIn(output))
    }

    private fun runBuild(gradleArgs: String) =
        runInShell(
            projectDir,
            "./gradlew --stacktrace --no-daemon",
            "-I ${tempDir / ResourceInitScripts.FORCE_SNAPSHOT_LIBRARY}",
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

    @Test
    fun testBuildPerformanceMetricsTaskWithOptions() {
        val output = runBuild("userBuildPerformanceMetrics --user runner --period=-1d").stdout
        assertPerformanceMetricsOutput(output, user = "runner", period = "-1d")
    }
}
