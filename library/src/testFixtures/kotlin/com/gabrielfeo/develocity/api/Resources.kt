package com.gabrielfeo.develocity.api

import java.net.URL
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div


@OptIn(ExperimentalPathApi::class)
fun Any.copyFromResources(path: String, targetDir: Path) {
    val sourcePath = Path.of(requireResource(path).toURI())
    val destPath = targetDir / path.removePrefix("/")
    destPath.createParentDirectories()
    sourcePath.copyToRecursively(destPath, followLinks = false, overwrite = true)
}

fun Any.requireResource(path: String): URL =
    requireNotNull(this::class.java.getResource(path))
