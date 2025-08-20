


import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import com.gabrielfeo.develocity.api.example.copyFromResources
import com.gabrielfeo.develocity.api.example.runInShell
import kotlin.io.path.div

class ExampleBuildLogicTest {

    @TempDir
    lateinit var tempDir: Path

    private val projectDir
        get() = tempDir / "examples/example-build-logic"

    @BeforeEach
    fun setup() {
        copyFromResources("/examples", tempDir)
    }

    @Test
    fun testBuildPerformanceMetricsTaskWithDefaults() {
        val user = System.getProperty("user.name")
        val output = runBuild("userBuildPerformanceMetrics")
        assertPerformanceMetricsOutput(output, user = user, period = "-14d")
    }

    private fun runBuild(gradleArgs: String) =
        runInShell(projectDir, "./gradlew --stacktrace --no-daemon $gradleArgs")

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
        val output = runBuild("userBuildPerformanceMetrics --user runner --period=-1d")
        assertPerformanceMetricsOutput(output, user = "runner", period = "-1d")
    }
}
