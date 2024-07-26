@file:Suppress("HasPlatformType")

plugins {
    base
}

// Cross-configure so we don't pollute the example buildscript
project("example-project").configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.gabrielfeo:develocity-api-kotlin"))
            .using(project(":library"))
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

val notebooksDir = file("example-notebooks")
val notebooks = fileTree(notebooksDir) { include("*.ipynb") }
val venvDir = project.layout.buildDirectory.asFile.map { File(it, "venv") }

val createPythonVenv by tasks.registering(Exec::class) {
    val requirements = File(notebooksDir, "requirements.txt")
    val venv = venvDir.get()
    commandLine(
        "bash", "-c",
        "python3 -m venv --upgrade-deps $venv "
            + "&& source $venv/bin/activate "
            + "&& pip install --upgrade pip"
            + "&& pip install -r $requirements"
    )
}

exampleTestTasks += notebooks.map { notebook ->
    val buildDir = project.layout.buildDirectory.asFile.get()
    tasks.register<Exec>("run${notebook.nameWithoutExtension}Notebook") {
        group = "Application"
        description = "Runs the '${notebook.name}' notebook with 'jupyter nbconvert --execute'"
        val venv = venvDir.get()
        dependsOn(createPythonVenv)
        commandLine(
            "bash", "-c",
            "source $venv/bin/activate "
                + "&& jupyter nbconvert --execute --to ipynb --output-dir='$buildDir' '$notebook'"
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
