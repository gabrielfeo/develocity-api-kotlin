package com.gabrielfeo.develocity.api.example.gradle

import com.gabrielfeo.develocity.api.example.Queries
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.div
import com.gabrielfeo.develocity.api.copyFromResources
import com.gabrielfeo.develocity.api.example.runInShell
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT

@Execution(CONCURRENT)
class ExampleProjectTest {

    class TestPaths(val rootDir: Path) {
        val projectDir = rootDir / "examples/example-project"
        val initScriptPath = rootDir / ResourceInitScripts.FORCE_SNAPSHOT_LIBRARY
    }

    @Test
    fun ensureRunBuildUsesSnapshotDependencies(@TempDir tempDir: Path) = with(setup(tempDir)) {
        val dependencies = runBuild("dependencies --configuration runtimeClasspath").stdout
        val libraryMatches = dependencies.lines().filter { "develocity-api-kotlin" in it }
        assertTrue(libraryMatches.isNotEmpty())
        assertTrue(libraryMatches.all { "-> SNAPSHOT" in it && "FAILED" !in it }) {
            "Expected forced SNAPSHOT versions, but found [${libraryMatches.joinToString(", ")}]"
        }
    }

    private fun setup(tempDir: Path): TestPaths {
        copyFromResources("/examples", tempDir)
        copyFromResources("/${ResourceInitScripts.FORCE_SNAPSHOT_LIBRARY}", tempDir)
        return TestPaths(tempDir)
    }

    @Test
    fun testExampleProject(@TempDir tempDir: Path) = with(setup(tempDir)) {
        val output = runBuild("""run --args '"${Queries.FAST}"'""").stdout
        val tableRegex = Regex("""(?ms)^[-]+\nMost frequent builds:\n\s*\n(.+\|\s*\d+\s*\n?)+""")
        assertTrue(tableRegex.containsMatchIn(output)) {
            "Expected match for pattern '$tableRegex' in output '$output'"
        }
    }

    private fun TestPaths.runBuild(gradleArgs: String) =
        runInShell(
            projectDir,
            "./gradlew --stacktrace --no-daemon",
            "-I $initScriptPath",
            gradleArgs,
        )
}
