@file:Suppress("HasPlatformType")

plugins {
    base
}

val exampleTestTasks = ArrayList<TaskProvider<*>>()

val snapshotTestingDir = project.layout.buildDirectory.dir("generated/snapshot-testing")
val mavenLocalUrl = System.getenv("HOME") + "/.m2/repository"
val scriptingSnapshotDirectives = """
    @file:Repository("$mavenLocalUrl")
    @file:DependsOn("com.gabrielfeo:gradle-enterprise-api-kotlin:SNAPSHOT")
""".trimIndent()

fun copyScriptReplacingVersion(original: File, dest: File) {
    dest.writer().buffered().use { writer ->
        original.forEachLine { line ->
            val edited = line.replace(
                Regex("""@file:DependsOn\("com.gabrielfeo:gradle-enterprise-api-kotlin:.*"\)"""),
                scriptingSnapshotDirectives,
            )
            writer.appendLine(edited)
        }
    }
}

// Add tasks to run each example script
val scripts = fileTree(file("example-scripts"))
exampleTestTasks += scripts.map { scriptFile ->
    val scriptName = scriptFile.name.substringBefore(".main.kts")
    @Suppress("DEPRECATION")
    val camelCaseName = scriptName.split("-").joinToString("") { it.capitalize() }
    tasks.register<Exec>("run${camelCaseName}Script") {
        group = "Application"
        description = """
            Runs the '${scriptFile.name}' script using a local version of the library, built and
            published to Maven Local.
        """.trimIndent()
        dependsOn(":library:publishUnsignedLibraryPublicationToMavenLocal")
        val snapshotScriptFile = snapshotTestingDir.map { it.file(scriptFile.relativeTo(projectDir).path) }
        inputs.file(scriptFile)
        doFirst {
            val testingDir = snapshotScriptFile.get().asFile.parentFile
            testingDir.deleteRecursively()
            testingDir.mkdirs()
            copyScriptReplacingVersion(scriptFile, snapshotScriptFile.get().asFile)
        }
        argumentProviders.add(CommandLineArgumentProvider { listOf(snapshotScriptFile.get().asFile.path) })
        commandLine("kotlinc", "-script")
        environment("JAVA_OPTS", "-Xmx1g")
    }
}

fun copyProject(original: File, dest: File) {
    val originalSource = fileTree(original) {
        exclude("build", ".gradle")
        filter { it.isFile }
    }
    // Delete dest and copy tree to dest
    dest.deleteRecursively()
    originalSource.files.forEach { file ->
        val relativePath = file.relativeTo(original)
        val destFile = dest.resolve(relativePath)
        destFile.parentFile.mkdirs()
        file.copyTo(destFile)
    }
}

fun replaceVersionInProject(project: File) {
    val buildscript = File(project, "app/build.gradle.kts")
    buildscript.writeText(
        buildscript.readText().replace(
            Regex("""implementation\("com.gabrielfeo:gradle-enterprise-api-kotlin:.*"\)"""),
            """
                implementation("com.gabrielfeo:gradle-enterprise-api-kotlin:SNAPSHOT")
                repositories {
                    exclusiveContent {
                        forRepository {
                            mavenLocal()
                        }
                        filter {
                            includeModule("com.gabrielfeo", "gradle-enterprise-api-kotlin")
                        }
                    }
                }
            """.trimIndent()
        )
    )
}

// Add a task to run the example-project
exampleTestTasks += tasks.register<GradleBuild>("runExampleProject") {
    group = "Application"
    description = """
        Runs examples/example-project as a standalone build with a local version of the library,
        built and published to Maven Local.
    """.trimIndent()
    setDir(snapshotTestingDir.map { it.dir("example-project").asFile })
    tasks = listOf("run")
    inputs.dir(file("example-project"))
    dependsOn(":library:publishUnsignedLibraryPublicationToMavenLocal")
    doFirst {
        copyProject(file("example-project"), dir)
        replaceVersionInProject(dir)
    }
}

fun copyNotebookReplacingVersion(original: File, dest: File) {
    dest.writer().buffered().use { writer ->
        original.forEachLine { line ->
            val directives = scriptingSnapshotDirectives + """
                import com.gabrielfeo.gradle.enterprise.api.*
                import com.gabrielfeo.gradle.enterprise.api.model.*
                import com.gabrielfeo.gradle.enterprise.api.extension.*
            """.trimIndent()
            val edited = line.replace(
                Regex(""""%use gradle-enterprise-api-kotlin.*?\\n""""),
                // The notebook source is a JSON
                directives.replace("\"", "\\\\\"").lines().joinToString(",\n") { """"$it\\n"""" },
            )
            writer.appendLine(edited)
        }
    }
}

// Add tasks to run each example notebook
val notebooks = fileTree(file("example-notebooks")) {
    exclude(".ipynb_checkpoints")
}
exampleTestTasks += notebooks.map { notebook ->
    val buildDir = project.layout.buildDirectory.asFile.get()
    tasks.register<Exec>("run${notebook.nameWithoutExtension}Notebook") {
        group = "Application"
        description = "Runs the '${notebook.name}' notebook with 'jupyter nbconvert --execute'"
        val snapshotNotebook = snapshotTestingDir.map { it.dir("notebooks").file(notebook.name).asFile }
        inputs.file(notebook)
        doFirst {
            snapshotNotebook.get().parentFile.mkdirs()
            snapshotNotebook.get().delete()
            copyNotebookReplacingVersion(notebook, snapshotNotebook.get())
        }
        argumentProviders.add(CommandLineArgumentProvider { listOf(snapshotNotebook.get().path) })
        commandLine(
            "jupyter", "nbconvert",
            "--execute",
            "--to", "ipynb",
            "--output-dir=$buildDir",
        )
    }
}

// Add a lifecycle task to run all the test tasks
val runAll = tasks.register("runAll") {
    group = "Application"
    description = "Runs everything in 'examples' directory"
    dependsOn(exampleTestTasks)
}

tasks.named("check") {
    dependsOn(runAll)
}
