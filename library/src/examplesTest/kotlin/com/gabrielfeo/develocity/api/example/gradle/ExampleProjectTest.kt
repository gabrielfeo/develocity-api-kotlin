package com.gabrielfeo.develocity.api.example.gradle

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.div
import com.gabrielfeo.develocity.api.example.copyFromResources
import com.gabrielfeo.develocity.api.example.runInShell

class ExampleProjectTest {

    @TempDir
    lateinit var tempDir: Path

    private val projectDir
        get() = tempDir / "examples/example-project"

    @BeforeEach
    fun setup() {
        copyFromResources("/examples", tempDir)
        forceUseOfMavenLocalSnapshotArtifact(projectDir / "build.gradle.kts")
    }

    @Test
    fun testExampleProject() {
        val output = runInShell(projectDir, "./gradlew --stacktrace --no-daemon run").trim()
        val tableRegex = Regex("""(?ms)^[-]+\nMost frequent builds:\n\s*\n(.+\|\s*\d+\s*\n?)+""")
        assertTrue(tableRegex.containsMatchIn(output)) {
            "Expected match for pattern '$tableRegex' in output '$output'"
        }
    }

    private fun forceUseOfMavenLocalSnapshotArtifact(buildGradlePath: Path) {
        val repositoryDefinition = """
            repositories {
                exclusiveContent {
                    forRepository { mavenLocal() }
                    filter { includeGroup("com.gabrielfeo") }
                }
            }
        """.trimIndent()
        val original = buildGradlePath.toFile().readText()
        val patched = original.replace(
            Regex("""com.gabrielfeo:develocity-api-kotlin:[^"]+"""),
            "com.gabrielfeo:develocity-api-kotlin:SNAPSHOT",
        ) + repositoryDefinition
        buildGradlePath.toFile().writeText(patched)
    }
}
