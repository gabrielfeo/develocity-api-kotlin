package com.gabrielfeo.develocity.api.example.script

import com.gabrielfeo.develocity.api.example.copyFromResources
import com.gabrielfeo.develocity.api.example.runInShell
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.div

class ScriptsTest {

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setup() {
        copyFromResources("/examples", tempDir)
    }

    @Test
    fun testMostFrequentBuildsScript() {
        val script = tempDir / "examples/example-scripts/example-script.main.kts"
        val replacedScript = forceUseOfMavenLocalSnapshotArtifact(script)
        val output = runInShell(tempDir, "kotlin '$replacedScript'").stdout.trim()
        val tableRegex = Regex("""(?ms)^[-]+\nMost frequent builds:\n\s*\n(.+\|\s*\d+\s*\n?)+""")
        assertTrue(tableRegex.containsMatchIn(output)) {
            "Expected match for pattern '$tableRegex' in output '$output'"
        }
    }

    /**
     * Replaces the dependency declaration in the script with a SNAPSHOT version and adds the maven local repository.
     */
    private fun forceUseOfMavenLocalSnapshotArtifact(scriptPath: Path): Path {
        val mavenLocal = checkNotNull(System.getProperty("user.home")).let { "$it/.m2/repository" }
        val scriptText = scriptPath.toFile().readText()
            val replaced = scriptText.replace(
                Regex("@file:DependsOn\\([^)]+\\)"),
                """
                    @file:DependsOn("com.gabrielfeo:develocity-api-kotlin:SNAPSHOT")
                    @file:Repository("file://$mavenLocal")
                """.trimIndent(),
        )
        val replacedPath = tempDir.resolve("examples/example-scripts/example-script-SNAPSHOT.main.kts")
        replacedPath.toFile().writeText(replaced)
        return replacedPath
    }
}
