plugins {
    id("com.gabrielfeo.kotlin-jvm-library") apply false
}

tasks.register("check") {
    dependsOn(gradle.includedBuilds.map { it.task(":check") })
}
