@file:Suppress("HasPlatformType")

plugins {
    base
}

// Cross-configure so we don't pollute the example buildscript
project("example-project").configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.gabrielfeo:gradle-enterprise-api-kotlin"))
            .using(project(":gradle-enterprise-api-kotlin"))
    }
}

val exampleTestTasks = ArrayList<TaskProvider<*>>()

exampleTestTasks += tasks.register<Exec>("runExampleScript") {
    group = "Application"
    description = "Runs the './example-scripts/example-script.main.kts' script"
    commandLine("kotlinc", "-script", file("./example-scripts/example-script.main.kts"))
    environment("JAVA_OPTS", "-Xmx1g")
}

exampleTestTasks += tasks.register("runExampleProject") {
    group = "Application"
    description = "Runs examples/example-project"
    dependsOn(":examples:example-project:run")
}

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

val runAll = tasks.register("runAll") {
    group = "Application"
    description = "Runs everything in 'examples' directory"
    dependsOn(exampleTestTasks)
}

tasks.named("check") {
    dependsOn(runAll)
}
