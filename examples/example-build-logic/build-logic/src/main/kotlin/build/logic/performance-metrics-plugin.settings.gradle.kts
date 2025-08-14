package build.logic

import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import kotlinx.coroutines.runBlocking

val apiService = gradle.sharedServices.registerIfAbsent("develocityApiService", DevelocityApiService::class)

configure<DevelocityConfiguration> {
    buildScan {
        // Required
        uploadInBackground = false
        buildScanPublished {
            runBlocking {
                // Print this build's performance metrics
                val metrics = getBuildScanPerformanceMetrics(apiService.get(), buildScanId)
                logger.lifecycle(metrics)
            }
        }
    }
}
