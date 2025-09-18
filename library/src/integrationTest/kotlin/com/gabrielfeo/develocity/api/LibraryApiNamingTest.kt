package com.gabrielfeo.develocity.api

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

class LibraryApiNamingTest {

    /**
     * Tests against unfixed cases of the enum member naming bug in openapi-generator.
     * See PostProcessGeneratedApi.kt.
     */
    @Test
    fun `all members are lower camel case`() {
        val libraryApi = readLibraryApi()
        val regex = Regex("""\b[a-z][A-Z]+\b""")
        val matches = regex.findAll(libraryApi).toList()
        assertTrue(matches.isEmpty(), "Lower camel case naming violations: ${matches.map { it.value }}")
    }

    private fun readLibraryApi(): String {
        val resource = this::class.java.classLoader.getResource("library.api")
        requireNotNull(resource) { "library.api not found in test resources" }
        return Files.readAllLines(Paths.get(resource.toURI())).joinToString("\n")
    }
}
