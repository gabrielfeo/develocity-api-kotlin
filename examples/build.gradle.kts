val exampleTestTasks = ArrayList<TaskProvider<*>>()

exampleTestTasks += tasks.register<Exec>("runExampleScript") {
    group = "Application"
    description = "Runs the 'example-script.main.kts' script"
    commandLine("kotlinc", "-script", file("example-script.main.kts"))
}

exampleTestTasks += tasks.register<GradleBuild>("checkExampleProject") {
    group = "Verification"
    description = "Checks examples/example-project as a standalone build"
    dir = file("example-project")
    tasks = listOf("check")
}

val notebooks = fileTree(file("example-notebooks")) {
    exclude(".ipynb_checkpoints")
}

exampleTestTasks += notebooks.map { notebook ->
    val buildDir = project.layout.buildDirectory.asFile.get()
    tasks.register<Exec>("run${notebook.nameWithoutExtension}Notebook") {
        group = "Application"
        description = "Runs the '${notebook.name}' notebook"
        commandLine(
            "jupyter", "nbconvert",
            "--execute",
            "--to", "ipynb",
            "--output-dir=$buildDir",
            notebook,
        )
    }
}

tasks.register("checkExamples") {
    group = "Verification"
    description = "Checks all examples"
    dependsOn(exampleTestTasks)
}
