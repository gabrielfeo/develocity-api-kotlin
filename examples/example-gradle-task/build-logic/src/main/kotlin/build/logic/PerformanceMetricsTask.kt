package build.logic

import com.gabrielfeo.develocity.api.DevelocityApi
import kotlinx.coroutines.runBlocking
import com.gabrielfeo.develocity.api.model.BuildModelName
import com.gabrielfeo.develocity.api.model.Build
import com.gabrielfeo.develocity.api.model.GradleBuildCachePerformance
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
        option = "user",
        description = "The user to query builds for. Defaults to the current OS username."
    )
    abstract val user: Property<String>

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
        val user = user.getOrElse(System.getProperty("user.name"))
        val startTime = period.getOrElse("-14d")
        val metrics = runBlocking {
            getUserBuildsPerformanceMetrics(api.get(), user, startTime)
        }
        logger.quiet(metrics)
    }

    suspend fun getUserBuildsPerformanceMetrics(
        api: DevelocityApi,
        user: String,
        startTime: String,
    ): String {
        val query = """user:"$user" buildStartTime>$startTime"""
        val buildsPerformanceData = fetchBuildsPerformanceData(api, query)
        val serializationFactors = buildsPerformanceData
            .map { it.serializationFactor }
            .let { DescriptiveStatistics(it.toDoubleArray()) }
        val avoidanceSavings = buildsPerformanceData
            .map { it.workUnitAvoidanceSavingsSummary.ratio }
            .let { DescriptiveStatistics(it.toDoubleArray()) }
        val heading = "Build performance overview for $user since $startTime (powered by Develocity®)"
        return """
            |
            |${"\u001B[1;36m"}$heading${"\u001B[0m"}
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

    private suspend fun fetchBuildsPerformanceData(
        api: DevelocityApi,
        query: String,
    ): List<GradleBuildCachePerformance> {
        return api.buildsApi.getBuilds(
            fromInstant = 0,
            query = query,
            models = listOf(BuildModelName.gradleBuildCachePerformance),
        ).mapNotNull { build ->
            build.models?.gradleBuildCachePerformance?.model
        }
    }
}
