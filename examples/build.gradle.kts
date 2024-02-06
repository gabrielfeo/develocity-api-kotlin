@file:Suppress("HasPlatformType")

plugins {
    base
}

val exampleTestTasks = ArrayList<TaskProvider<*>>()
val snapshotTestingDir = project.layout.buildDirectory.dir("generated/snapshot-testing")

fun copyScriptReplacingVersion(original: File, dest: File) {
    val mavenLocalUrl = System.getenv("HOME") + "/.m2/repository"
    dest.writer().buffered().use { writer ->
        original.forEachLine { line ->
            val edited = line.replace(
                Regex("""@file:DependsOn\("com.gabrielfeo:gradle-enterprise-api-kotlin:.*"\)"""),
                """
                    @file:Repository("$mavenLocalUrl")
                    @file:DependsOn("com.gabrielfeo:gradle-enterprise-api-kotlin:SNAPSHOT")
                """.trimIndent()
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

// Add a task to run the example-project
exampleTestTasks += tasks.register<GradleBuild>("runExampleProject") {
    group = "Application"
    description = "Runs examples/example-project as a standalone build"
    dir = file("example-project")
    tasks = listOf("run")
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
        commandLine(
            "jupyter", "nbconvert",
            "--execute",
            "--to", "ipynb",
            "--output-dir=$buildDir",
            notebook,
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
