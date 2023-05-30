val exampleTestTasks = ArrayList<TaskProvider<*>>()

exampleTestTasks += tasks.register<Exec>("runExampleScript") {
    group = "Application"
    description = "Runs the 'example-script.main.kts' script"
    commandLine("kotlinc", "-script", file("example-script.main.kts"))
}

exampleTestTasks += tasks.register<GradleBuild>("runExampleProject") {
    group = "Application"
    description = "Runs examples/example-project as a standalone build"
    dir = file("example-project")
    tasks = listOf("run")
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

tasks.register("runAll") {
    group = "Application"
    description = "Runs everything in 'examples' directory"
    dependsOn(exampleTestTasks)
}
