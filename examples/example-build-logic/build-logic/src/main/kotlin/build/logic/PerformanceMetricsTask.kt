package build.logic

import com.gabrielfeo.develocity.api.DevelocityApi
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.services.ServiceReference
import java.time.Duration

abstract class PerformanceMetricsTask(

) : DefaultTask() {

    @get:Input
    @get:Option(
        option = "build-scan-id",
        description = "The ID of the build scan to retrieve performance metrics for."
    )
    abstract val buildScanId: Property<String>

    @get:ServiceReference
    abstract val api: Property<DevelocityApiService>

    @TaskAction
    fun run() {
        val metrics = runBlocking {
            getBuildScanPerformanceMetrics(api.get(), buildScanId.get())
        }
        logger.quiet(metrics)
    }

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
        |
        |${"\u001B[1;36m"}Execution phase performance overview (powered by Develocity®):${"\u001B[0m"}
        |  1️⃣  Serial execution time: ${perf.serialWorkUnitExecutionTime}
        |  2️⃣  Wall-clock execution time: ${perf.effectiveWorkUnitExecutionTime}
        |      ▶︎  ${perf.serializationFactor}x faster thanks to Gradle's parallel execution
        |      ⏩︎ ${perf.workUnitAvoidanceSavingsSummary.ratio}% faster thanks to Gradle and Develocity's caching mechanisms
    """.trimMargin()
    }
}
