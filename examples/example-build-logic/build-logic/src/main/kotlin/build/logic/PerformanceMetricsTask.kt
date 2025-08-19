package build.logic

import com.gabrielfeo.develocity.api.DevelocityApi
import kotlinx.coroutines.runBlocking
import com.gabrielfeo.develocity.api.model.BuildModelName
import com.gabrielfeo.develocity.api.model.Build
import com.gabrielfeo.develocity.api.model.GradleBuildCachePerformance
import kotlin.math.roundToInt
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
        val metrics = runBlocking {
            val startTime = period.orNull?.takeIf { it.isNotBlank() } ?: "-14d"
            getUserBuildsPerformanceMetrics(api.get(), startTime)
        }
        logger.quiet(metrics)
    }

    suspend fun getUserBuildsPerformanceMetrics(api: DevelocityApi, startTime: String): String {
        val user = System.getProperty("user.name")
        val builds = api.buildsApi.getBuilds(
            fromInstant = 0,
            query = """user:"$user" buildStartTime>$startTime""",
            models = listOf(BuildModelName.gradleBuildCachePerformance),
        )
        if (builds.isEmpty()) return "No builds found for user $user."
        val performances = builds.mapNotNull { build -> build.models?.gradleBuildCachePerformance?.model }
        if (performances.isEmpty()) return "No Gradle build cache performance data found for user $user."

        fun List<Double>.mean() = if (isEmpty()) 0.0 else sum() / size
        fun List<Double>.p95(): Double {
            if (isEmpty()) return 0.0
            val sorted = sorted()
            val idx = (size * 0.95).roundToInt().coerceAtMost(size - 1)
            return sorted[idx]
        }

        val serializationFactors = performances.map { it.serializationFactor }
        val avoidanceSavings = performances.mapNotNull { it.workUnitAvoidanceSavingsSummary?.ratio }

        return """
            |${"\u001B[1;36m"}User build performance overview (powered by Develocity®):${"\u001B[0m"}
            |  ▶︎ Serialization factor: ${serializationFactors.mean()} (mean) ~ ${serializationFactors.p95()} (p95)
            |      (Gradle's parallel execution)
            |  ⏩︎ Avoidance savings: ${avoidanceSavings.mean()}% (mean) ~ ${avoidanceSavings.p95()}% (p95)
            |      (Gradle and Develocity's mechanisms, incl. incremental build and remote cache)
        """.trimMargin()
    }
}
