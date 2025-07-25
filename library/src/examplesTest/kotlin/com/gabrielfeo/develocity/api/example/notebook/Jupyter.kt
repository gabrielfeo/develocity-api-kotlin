package com.gabrielfeo.develocity.api.example.notebook

import com.gabrielfeo.develocity.api.example.copyFromResources
import com.gabrielfeo.develocity.api.example.runInShell
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists

class Jupyter(
    val workDir: Path,
    val venv: Path,
) {

    fun executeNotebook(path: Path): Path {
        val outputPath = path.parent / "${path.nameWithoutExtension}-executed.ipynb"
        runInShell(
            workDir,
            "source '${venv / "bin/activate"}' &&",
            "jupyter nbconvert '$path'",
            "--to ipynb",
            "--execute",
            "--output='$outputPath'",
        )
        return outputPath
    }

    fun replaceMagics(
        path: Path,
        replacePattern: Regex,
        replacement: String
    ): Path {
        if ((workDir / "preprocessors.py").notExists()) {
            copyFromResources("/preprocessors.py", workDir)
        }
        val outputPath = path.parent / "${path.nameWithoutExtension}-processed.ipynb"
        runInShell(
            workDir,
            "source '${venv / "bin/activate"}' &&",
            "jupyter nbconvert '$path'",
            "--to ipynb",
            "--output='$outputPath'",
            "--NotebookExporter.preprocessors=preprocessors.ReplaceMagicsPreprocessor",
            "--ReplaceMagicsPreprocessor.pattern='$replacePattern'",
            "--ReplaceMagicsPreprocessor.replacement='$replacement'",
        )
        return outputPath
    }
}