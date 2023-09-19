plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10" apply false
    id("org.jetbrains.dokka") version "1.9.0" apply false
    id("org.openapi.generator") version "7.0.1" apply false
}

val group by project.properties
val artifact by project.properties

project(":examples:example-project:app").configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("$group:$artifact"))
            .using(project(":library"))
    }
}
