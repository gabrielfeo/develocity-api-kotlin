package com.gabrielfeo.develocity.api.example

import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.file.Path

fun runInShell(workDir: Path, vararg command: String) =
    runInShell(workDir, command.joinToString(" "))

fun runInShell(workDir: Path, command: String): OutputStreams {
    val process = ProcessBuilder("bash", "-c", command).apply {
        directory(workDir.toFile())
        // Ensure the test's build toolchain is used (not whatever JAVA_HOME is set to)
        environment()["JAVA_HOME"] = System.getProperty("java.home")
    }.start()
    val streams = runBlocking {
        OutputStreams(
            stderr = async(start = UNDISPATCHED) {
                process.errorStream.bufferedReader().lineSequence()
                    .onEach(System.err::println)
                    .joinToString("\n")
            }.await(),
            stdout = async(start = UNDISPATCHED) {
                process.inputStream.bufferedReader().lineSequence()
                    .onEach(System.out::println)
                    .joinToString("\n")
            }.await(),
        )
    }
    val exitCode = process.waitFor()
    check(exitCode == 0) { "Exit code '$exitCode' for command: $command" }
    return streams
}

class OutputStreams(val stdout: String, val stderr: String)
