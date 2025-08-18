package build.logic

import kotlinx.coroutines.runBlocking

gradle.sharedServices.registerIfAbsent("develocityApiService", DevelocityApiService::class)

tasks.register<PerformanceMetricsTask>("buildPerformanceMetrics") {
    group = "Develocity"
    description = "Retrieves performance metrics of a Gradle Build Scan"
}
