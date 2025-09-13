package com.gabrielfeo.develocity.api.example.gradle

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import com.gabrielfeo.develocity.api.copyFromResources
import com.gabrielfeo.develocity.api.example.BuildStartTime
import com.gabrielfeo.develocity.api.example.runInShell
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import kotlin.io.path.div

@Execution(CONCURRENT)
class ExampleGradleTaskTest {

    class TestPaths(val rootDir: Path) {
        val initScriptsDir = rootDir
        val projectDir = rootDir / "examples/example-gradle-task"
    }

    @Test
    fun ensureRunBuildUsesSnapshotDependency(@TempDir tempDir: Path) = with(setup(tempDir)) {
        val dependencies = runBuild(":buildSrc:dependencies --configuration runtimeClasspath").stdout
        val libraryMatches = dependencies.lines().filter { "develocity-api-kotlin" in it }
        assertTrue(libraryMatches.isNotEmpty())
        assertTrue(libraryMatches.all { "-> SNAPSHOT" in it && "FAILED" !in it }) {
            "Expected forced SNAPSHOT versions, but found [${libraryMatches.joinToString(", ")}]"
        }
    }

    private fun setup(tempDir: Path): TestPaths {
        copyFromResources("/examples", tempDir)
        copyFromResources("/${ResourceInitScripts.FORCE_SNAPSHOT_LIBRARY}", tempDir)
        copyFromResources("/${ResourceInitScripts.REQUIRE_JAVA_11_COMPATIBILITY}", tempDir)
        return TestPaths(tempDir)
    }

    @Test
    fun testBuildPerformanceMetricsTask(@TempDir tempDir: Path) = with(setup(tempDir)) {
        val args = "--user runner --period=${BuildStartTime.RECENT}"
        val output = runBuild("userBuildPerformanceMetrics $args").stdout
        assertPerformanceMetricsOutput(output, user = "runner", period = BuildStartTime.RECENT)
    }

    @Test
    fun testJavaVersionCompatibility(@TempDir tempDir: Path) = with(setup(tempDir)) {
        val initScript = initScriptsDir / ResourceInitScripts.REQUIRE_JAVA_11_COMPATIBILITY
        val output = runBuild("-p buildSrc :generateExternalPluginSpecBuilders -I '$initScript'").stdout
        assertFalse(Regex("""FAILED|Could not resolve|No matching variant""").containsMatchIn(output))
    }

    private fun TestPaths.runBuild(gradleArgs: String) =
        runInShell(
            projectDir,
            "./gradlew --stacktrace --no-daemon",
            "-I ${initScriptsDir / ResourceInitScripts.FORCE_SNAPSHOT_LIBRARY}",
            gradleArgs,
        )

    @Suppress("SameParameterValue")
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
