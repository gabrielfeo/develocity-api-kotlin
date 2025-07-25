package com.gabrielfeo.develocity.api.example.notebook

import com.gabrielfeo.develocity.api.example.runInShell
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.div

class PythonVenv(
    val dir: Path,
) {

    init {
        runInShell(dir.parent, "python3 -m venv '$dir'")
    }

    fun installRequirements(requirementsFile: Path, linksDir: Path) {
        runInShell(
            dir.parent,
            "source '${dir / "bin/activate"}' &&",
            "pip install",
            "-r '$requirementsFile'",
            "--no-index",
            "--find-links ${linksDir.absolute()}",
        )
    }
}