tasks.register("check") {
  dependsOn(gradle.includedBuilds.map { it.task(":check") })
}
