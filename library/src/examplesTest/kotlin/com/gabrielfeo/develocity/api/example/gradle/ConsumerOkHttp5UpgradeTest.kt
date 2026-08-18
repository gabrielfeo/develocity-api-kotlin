package com.gabrielfeo.develocity.api.example.gradle

import com.gabrielfeo.develocity.api.copyFromResources
import com.gabrielfeo.develocity.api.example.Queries
import com.gabrielfeo.develocity.api.example.runInShell
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.nio.file.Path
import kotlin.io.path.div

/*
 * The library declares (and is tested against) okhttp 4.x, but a consumer may have
 * independently bumped their own build to okhttp 5.x. These tests layer that upgrade
 * on top of example-project's unmodified build, to verify the library still works
 * correctly when only a newer okhttp jar is present on the consumer's classpath.
 */
@Execution(CONCURRENT)
class ConsumerOkHttp5UpgradeTest {

    class TestPaths(val rootDir: Path) {
        val projectDir = rootDir / "examples/example-project"
        val forceSnapshotLibraryInitScript = rootDir / ResourceInitScripts.FORCE_SNAPSHOT_LIBRARY
        val forceConsumerOkHttp5InitScript = rootDir / ResourceInitScripts.FORCE_CONSUMER_OKHTTP_5
    }

    private fun setup(tempDir: Path): TestPaths {
        copyFromResources("/examples", tempDir)
        copyFromResources("/${ResourceInitScripts.FORCE_SNAPSHOT_LIBRARY}", tempDir)
        copyFromResources("/${ResourceInitScripts.FORCE_CONSUMER_OKHTTP_5}", tempDir)
        return TestPaths(tempDir)
    }

    @Test
    fun ensureConsumerResolvesToOkHttp5(@TempDir tempDir: Path) = with(setup(tempDir)) {
        val dependencies = runBuild("dependencies --configuration runtimeClasspath").stdout
        val okHttpRequestLines = dependencies.lines().filter { "com.squareup.okhttp3:okhttp:" in it }
        assertTrue(okHttpRequestLines.isNotEmpty()) {
            "Expected an okhttp entry in the runtime classpath, found none in:\n$dependencies"
        }
        assertTrue(okHttpRequestLines.all { "-> 5.4.0" in it }) {
            "Expected okhttp forced to 5.4.0, but found [${okHttpRequestLines.joinToString(", ")}]"
        }
        assertTrue("com.squareup.okhttp3:okhttp-jvm:5.4.0" in dependencies) {
            "Expected the resolved classpath to contain okhttp-jvm:5.4.0, found none in:\n$dependencies"
        }
    }

    @Test
    fun testExampleProjectWithConsumerOkHttp5(@TempDir tempDir: Path) = with(setup(tempDir)) {
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
            "-I $forceSnapshotLibraryInitScript",
            "-I $forceConsumerOkHttp5InitScript",
            gradleArgs,
        )
}
