package build.logic

import kotlinx.coroutines.runBlocking

gradle.sharedServices.registerIfAbsent("develocityApiService", DevelocityApiService::class)

tasks.register<PerformanceMetricsTask>("userBuildPerformanceMetrics") {
    group = "Develocity"
    description = "Retrieves performance metrics for the user's builds from Develocity API."
}
