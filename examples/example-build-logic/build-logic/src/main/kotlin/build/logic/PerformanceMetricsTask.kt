package build.logic

import com.gabrielfeo.develocity.api.DevelocityApi
import kotlinx.coroutines.runBlocking
import com.gabrielfeo.develocity.api.model.BuildModelName
import com.gabrielfeo.develocity.api.model.Build
import com.gabrielfeo.develocity.api.model.GradleBuildCachePerformance
import kotlin.math.roundToInt
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.services.ServiceReference
import java.time.Duration


abstract class PerformanceMetricsTask(

) : DefaultTask() {

    @get:Optional
    @get:Input
    @get:Option(
        option = "period",
        description = "The period to query builds for (e.g. -14d, -30d, etc). Default: -14d."
    )
    abstract val period: Property<String>

    @get:ServiceReference
    abstract val api: Property<DevelocityApiService>

    @TaskAction
    fun run() {
        val startTime = period.orNull?.takeIf { it.isNotBlank() } ?: "-14d"
        val metrics = runBlocking {
            getUserBuildsPerformanceMetrics(api.get(), startTime)
        }
        logger.quiet(metrics)
    }

    suspend fun getUserBuildsPerformanceMetrics(api: DevelocityApi, startTime: String): String {
        val user = System.getProperty("user.name")
        val buildsPerformanceData = api.buildsApi.getBuilds(
            fromInstant = 0,
            query = """user:"$user" buildStartTime>$startTime""",
            models = listOf(BuildModelName.gradleBuildCachePerformance),
        ).mapNotNull { build ->
            build.models?.gradleBuildCachePerformance?.model
        }

        val serializationFactors = buildsPerformanceData
            .map { it.serializationFactor }
            .let { DescriptiveStatistics(it.toDoubleArray()) }

        val avoidanceSavings = buildsPerformanceData
            .map { it.workUnitAvoidanceSavingsSummary.ratio }
            .let { DescriptiveStatistics(it.toDoubleArray()) }

        return """
            |${"\u001B[1;36m"}User build performance overview (powered by Develocity®):${"\u001B[0m"}
            |  ▶︎ Serialization factor: %.1fx
            |      (Gradle's parallel execution)
            |  ⏩︎ Avoidance savings: %.1f%% (mean) ~ %.1f%% (p95)
            |      (Gradle and Develocity's mechanisms, incl. incremental build and remote cache)
        """.trimMargin().format(
            serializationFactors.mean,
            avoidanceSavings.mean,
            avoidanceSavings.getPercentile(95.0),
        )
    }
}
