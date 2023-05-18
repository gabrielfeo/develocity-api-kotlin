plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.21" apply false
    id("org.jetbrains.dokka") version "1.8.10" apply false
    id("org.openapi.generator") version "6.5.0" apply false
}

val group by project.properties
val artifact by project.properties

project(":examples:example-project:app").configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("$group:$artifact"))
            .using(project(":library"))
    }
}
