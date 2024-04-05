@file:Suppress("HasPlatformType")

import com.gabrielfeo.task.ForceNotebooksToUseSnapshot


plugins {
    base
    id("com.gabrielfeo.examples-testing")
}

// Cross-configure so we don't pollute the example buildscript
project("example-project").configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.gabrielfeo:develocity-api-kotlin"))
            .using(project(":library"))
    }
}

val originalScripts = fileTree(file("example-scripts"))

val useDeclaredVersion = providers.gradleProperty("useDeclaredVersion")
    .map { it.toBoolean() }
    .orElse(false)

// TODO scripts
val scripts = originalScripts
val notebooks = fileTree("example-notebooks") {
    exclude(".ipynb_checkpoints")
}

val exampleTestTasks = ArrayList<TaskProvider<*>>()

exampleTestTasks += scripts.map { script ->
    tasks.register<Exec>("runExampleScript") {
        group = "Application"
        description = "Runs the './example-scripts/${script.name}' script"
        commandLine("kotlinc", "-script", script)
        environment("JAVA_OPTS", "-Xmx1g")
    }
}

exampleTestTasks += tasks.register("runExampleProject") {
    group = "Application"
    description = "Runs examples/example-project"
    dependsOn(":examples:example-project:run")
}

exampleTestTasks += notebooks.map { notebook ->
    val notebookName = notebook.nameWithoutExtension
    val force = tasks.register("force${notebookName}ToSnapshot", ForceNotebooksToUseSnapshot::class) {
        originalNotebook.set(notebook)
        modifiedNotebook.set(project.layout.buildDirectory.file("modified/${notebook.name}"))
        version.set(providers.gradleProperty("version"))
        dependsOn(":library:publishUnsignedDevelocityApiKotlinPublicationToMavenLocal")
    }
    tasks.register<Exec>("run${notebookName}NotebookWithSnapshot") {
        group = "Application"
        description = "Runs '${notebook.name}' with 'jupyter nbconvert --execute' with snapshot code"
        commandLine(
            "jupyter", "nbconvert",
            "--execute",
            "--to", "ipynb",
            project.layout.buildDirectory.map { "--output-dir=${it.asFile}" },
            force.map { it.modifiedNotebook.get().asFile },
        )
    }
    tasks.register<Exec>("run${notebookName}Notebook") {
        group = "Application"
        description = "Runs '${notebook.name}' with 'jupyter nbconvert --execute'"
        commandLine(
            "jupyter", "nbconvert",
            "--execute",
            "--to", "ipynb",
            project.layout.buildDirectory.map { "--output-dir=${it.asFile}" },
            notebook,
        )
    }
}

val runAll = tasks.register("runAll") {
    group = "Application"
    description = "Runs everything in 'examples' directory"
    dependsOn(exampleTestTasks)
}

tasks.named("check") {
    dependsOn(runAll)
}
