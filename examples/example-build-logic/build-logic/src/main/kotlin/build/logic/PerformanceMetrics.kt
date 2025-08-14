package build.logic

import com.gabrielfeo.develocity.api.DevelocityApi
import java.time.Duration

suspend fun getBuildScanPerformanceMetrics(
    api: DevelocityApi,
    buildScanId: String,
    timeout: Duration = Duration.ofSeconds(60),
): String {
    val perf = api.buildsApi.getGradleBuildCachePerformance(
        id = buildScanId,
        availabilityWaitTimeoutSecs = timeout.toSecondsPart(),
    )
    return """
        |${"\u001B[1;36m"}Execution phase performance overview (powered by Develocity®):${"\u001B[0m"}
        |  1️⃣Serial execution time: ${perf.serialWorkUnitExecutionTime}
        |  2️⃣Wall-clock execution time: ${perf.effectiveWorkUnitExecutionTime}
        |    ▶︎ ${perf.serializationFactor}x faster thanks to Gradle's parallel execution
        |    ⏩︎ ${perf.workUnitAvoidanceSavingsSummary.ratio}% faster thanks to Gradle and Develocity's caching mechanisms
    """.trimMargin()
}