package com.gabrielfeo.develocity.api.example

import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div


@OptIn(ExperimentalPathApi::class)
fun Any.copyFromResources(path: String, targetDir: Path) {
    val examples = requireNotNull(this::class.java.getResource(path))
    val sourcePath = Path.of(examples.toURI())
    val destPath = targetDir / path.removePrefix("/")
    destPath.createParentDirectories()
    sourcePath.copyToRecursively(destPath, followLinks = false, overwrite = true)
}