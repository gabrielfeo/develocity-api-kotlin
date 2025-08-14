


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
    fun testExampleBuildLogic() {
        val output = runInShell(projectDir, "./gradlew --stacktrace --no-daemon help")
        assertTrue(output.contains("Execution phase performance overview"))
    }
}
