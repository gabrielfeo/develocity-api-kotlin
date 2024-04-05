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

val originalScripts = fileTree(file("example-scripts"))
val originalNotebooks = fileTree(file("example-notebooks")) {
    exclude(".ipynb_checkpoints")
}

val useDeclaredVersion = providers.gradleProperty("useDeclaredVersion").orNull.toBoolean()
val version = providers.gradleProperty("version").get()

val mavenLocalUrl = System.getenv("HOME") + "/.m2/repository"
val scriptingSnapshotDirectives = """
    @file:Repository("$mavenLocalUrl")
    @file:DependsOn("com.gabrielfeo:develocity-api-kotlin:SNAPSHOT")
""".trimIndent()

// TODO scripts
val scripts = originalScripts
val notebooks = if (useDeclaredVersion) {
    originalNotebooks
} else {
    val newNotebooks = project.layout.buildDirectory.dir("modified-notebooks").get().asFile
    newNotebooks.deleteRecursively()
    copy {
        from(file("jupyter-maven-local-descriptor.json"))
        into(newNotebooks)
        filter { l ->
            when {
                "{{HOME}}" in l -> l.replace("{{HOME}}", System.getProperty("user.home"))
                "{{VERSION}}" in l -> l.replace("{{VERSION}}", version)
                else -> l
            }
        }
    }
    val newDescriptor = newNotebooks.resolve("jupyter-maven-local-descriptor.json")
    copy {
        from(originalNotebooks)
        into(newNotebooks)
        filter { l ->
            if ("%use develocity-api-kotlin" in l) l.replace("%use develocity-api-kotlin", "%use @$newDescriptor")
                .replace("(version=2023.4.0)", "")
            else l
        }
    }
    fileTree(newNotebooks)
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
        if (!useDeclaredVersion) {
            dependsOn(":library:publishUnsignedDevelocityApiKotlinPublicationToMavenLocal")
        }
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
