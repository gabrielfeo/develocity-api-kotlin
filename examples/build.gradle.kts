@file:Suppress("HasPlatformType")

plugins {
    base
}

// Cross-configure so we don't pollute the example buildscript
project("example-project") {
    apply(plugin = "com.gabrielfeo.kotlin-jvm-library")
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute(module("com.gabrielfeo:develocity-api-kotlin"))
                .using(project(":library"))
        }
    }
}

val exampleTestTasks = ArrayList<TaskProvider<*>>()

exampleTestTasks += tasks.register("runExampleProject") {
    group = "Application"
    description = "Runs examples/example-project"
    dependsOn(":examples:example-project:run")
}

val runAll = tasks.register("runAll") {
    group = "Application"
    description = "Runs everything in 'examples' directory, except for notebooks (moved to exampleTests suite)"
    dependsOn(exampleTestTasks)
}

tasks.named("check") {
    dependsOn(runAll)
}
