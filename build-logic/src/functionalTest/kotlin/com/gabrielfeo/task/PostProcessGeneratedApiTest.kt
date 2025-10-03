package com.gabrielfeo.task

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class PostProcessGeneratedApiTest {

    // Prefer over TempDir to inspect files after tests when troubleshooting
    private val tempDir: File = File("./build/test-workdir/")

    @BeforeEach
    fun setup() {
        tempDir.deleteRecursively()
        tempDir.mkdirs()
    }

    /**
     * - Fixes missing model imports by replacing all with a wildcard (OpenAPITools/openapi-generator#14871)
     * - Adds @JvmSuppressWildcards to avoid square/retrofit#3275
     */
    @Test
    fun apiInterfacePostProcessing() = testPostProcessing(
        inputPath = "src/main/kotlin/com/gabrielfeo/develocity/api/BuildsApi.kt",
        inputContent = """
            package com.gabrielfeo.develocity.api

            import com.gabrielfeo.develocity.api.internal.infrastructure.CollectionFormats.*
            import retrofit2.http.*
            import okhttp3.RequestBody
            import com.squareup.moshi.Json

            import com.gabrielfeo.develocity.api.model.ApiProblem
            import com.gabrielfeo.develocity.api.model.Build
            import com.gabrielfeo.develocity.api.model.BuildModelQuery
            import com.gabrielfeo.develocity.api.model.BuildQuery
            import com.gabrielfeo.develocity.api.model.BuildsQuery
            import com.gabrielfeo.develocity.api.model.GradleAttributes
            import com.gabrielfeo.develocity.api.model.GradleBuildCachePerformance
            import com.gabrielfeo.develocity.api.model.GradleNetworkActivity
            import com.gabrielfeo.develocity.api.model.GradleProject
            import com.gabrielfeo.develocity.api.model.MavenAttributes
            import com.gabrielfeo.develocity.api.model.MavenBuildCachePerformance
            import com.gabrielfeo.develocity.api.model.MavenDependencyResolution
            import com.gabrielfeo.develocity.api.model.MavenModule

            import com.gabrielfeo.develocity.api.model.*

            interface BuildsApi {
                /**
                 * Get the common attributes of a Build Scan.
                 * The contained attributes are build tool agnostic.
                 * Responses:
                 *  - 200: The common attributes of a Build Scan.
                 *  - 400: The request cannot be fulfilled due to a problem.
                 *  - 404: The referenced resource either does not exist or the permissions to know about it are missing.
                 *  - 500: The server encountered an unexpected error.
                 *  - 503: The server is not ready to handle the request.
                 *
                 * @param id The Build Scan ID.
                 * @param models The list of build models to return in the response for each build. If not provided, no models are returned.  (optional)
                 * @param availabilityWaitTimeoutSecs The time in seconds the server should wait for ingestion before returning a wait timeout response. (optional)
                 * @return [Build]
                 */
                @GET("api/builds/{id}")
                suspend fun getBuild(@Path("id") id: kotlin.String, @Query("models") models: kotlin.collections.List<BuildModelName>? = null, @Query("availabilityWaitTimeoutSecs") availabilityWaitTimeoutSecs: kotlin.Int? = null): Build
        """.trimIndent(),
        outputPath = "src/main/kotlin/com/gabrielfeo/develocity/api/BuildsApi.kt",
        outputContent = """
            package com.gabrielfeo.develocity.api

            import com.gabrielfeo.develocity.api.internal.infrastructure.CollectionFormats.*
            import retrofit2.http.*
            import okhttp3.RequestBody
            import com.squareup.moshi.Json

            import com.gabrielfeo.develocity.api.model.ApiProblem
            import com.gabrielfeo.develocity.api.model.Build
            import com.gabrielfeo.develocity.api.model.BuildModelQuery
            import com.gabrielfeo.develocity.api.model.BuildQuery
            import com.gabrielfeo.develocity.api.model.BuildsQuery
            import com.gabrielfeo.develocity.api.model.GradleAttributes
            import com.gabrielfeo.develocity.api.model.GradleBuildCachePerformance
            import com.gabrielfeo.develocity.api.model.GradleNetworkActivity
            import com.gabrielfeo.develocity.api.model.GradleProject
            import com.gabrielfeo.develocity.api.model.MavenAttributes
            import com.gabrielfeo.develocity.api.model.MavenBuildCachePerformance
            import com.gabrielfeo.develocity.api.model.MavenDependencyResolution
            import com.gabrielfeo.develocity.api.model.MavenModule

            import com.gabrielfeo.develocity.api.model.*

            @JvmSuppressWildcards
            interface BuildsApi {
                /**
                 * Get the common attributes of a Build Scan.
                 * The contained attributes are build tool agnostic.
                 * Responses:
                 *  - 200: The common attributes of a Build Scan.
                 *  - 400: The request cannot be fulfilled due to a problem.
                 *  - 404: The referenced resource either does not exist or the permissions to know about it are missing.
                 *  - 500: The server encountered an unexpected error.
                 *  - 503: The server is not ready to handle the request.
                 *
                 * @param id The Build Scan ID.
                 * @param models The list of build models to return in the response for each build. If not provided, no models are returned.  (optional)
                 * @param availabilityWaitTimeoutSecs The time in seconds the server should wait for ingestion before returning a wait timeout response. (optional)
                 * @return [Build]
                 */
                @GET("api/builds/{id}")
                suspend fun getBuild(@Path("id") id: kotlin.String, @Query("models") models: kotlin.collections.List<BuildModelName>? = null, @Query("availabilityWaitTimeoutSecs") availabilityWaitTimeoutSecs: kotlin.Int? = null): Build
        """.trimIndent(),
    )

    /**
     * - Fixes enum case names: gradleMinusAttributes -> gradleAttributes
     */
    @Test
    fun buildModelNameEnumPostProcessing() = testPostProcessing(
        inputPath = "src/main/kotlin/com/gabrielfeo/develocity/api/model/BuildModelName.kt",
        inputContent = """
            @JsonClass(generateAdapter = false)
            enum class BuildModelName(val value: kotlin.String) {

                @Json(name = "gradle-attributes")
                gradleMinusAttributes("gradle-attributes"),

                @Json(name = "gradle-build-cache-performance")
                gradleMinusBuildMinusCacheMinusPerformance("gradle-build-cache-performance"),
        """.trimIndent(),
        outputPath = "src/main/kotlin/com/gabrielfeo/develocity/api/model/BuildModelName.kt",
        outputContent = """
            @JsonClass(generateAdapter = false)
            enum class BuildModelName(val value: kotlin.String) {

                @Json(name = "gradle-attributes")
                gradleAttributes("gradle-attributes"),

                @Json(name = "gradle-build-cache-performance")
                gradleBuildCachePerformance("gradle-build-cache-performance"),
        """.trimIndent(),
    )

    /**
     * Fixes enum case names: hIT -> hit (gabrielfeo/develocity-api-kotlin#282).
     *
     * Occurs when API spec enum name is uppercase and generator enumPropertyNaming is camelCase.
     */
    @Test
    fun gradleConfigurationCacheResultOutcomeEnumPostProcessing() = testPostProcessing(
        inputPath = "src/main/kotlin/com/gabrielfeo/develocity/api/model/GradleConfigurationCacheResult.kt",
        inputContent = """
            /**
             * The outcome of the configuration cache operation:   * `HIT` - There was a configuration cache hit.   * `MISS` - There was a configuration cache miss.   * `FAILED` - There was a configuration cache related failure.
             *
             * Values: hIT,mISS,fAILED
             */
            @JsonClass(generateAdapter = false)
            enum class Outcome(val value: kotlin.String) {
                @Json(name = "HIT") hIT("HIT"),
                @Json(name = "MISS") mISS("MISS"),
                @Json(name = "FAILED") fAILED("FAILED");
            }
        """.trimIndent(),
        outputPath = "src/main/kotlin/com/gabrielfeo/develocity/api/model/GradleConfigurationCacheResult.kt",
        outputContent = """
            /**
             * The outcome of the configuration cache operation:   * `HIT` - There was a configuration cache hit.   * `MISS` - There was a configuration cache miss.   * `FAILED` - There was a configuration cache related failure.
             *
             * Values: hit,miss,failed
             */
            @JsonClass(generateAdapter = false)
            enum class Outcome(val value: kotlin.String) {
                @Json(name = "HIT") hit("HIT"),
                @Json(name = "MISS") miss("MISS"),
                @Json(name = "FAILED") failed("FAILED");
            }
        """.trimIndent(),
    )

    private fun testPostProcessing(
        inputPath: String,
        inputContent: String,
        outputPath: String,
        outputContent: String,
    ) {
        val inputDir = File(tempDir, "input").also { it.mkdirs() }
        val outputDir = File(tempDir, "output").also { it.mkdirs() }
        File(inputDir, inputPath).apply {
            parentFile.mkdirs()
            writeText(inputContent)
        }
        val projectDir = writeTestProject(inputDir, outputDir)
        runBuild(projectDir, listOf("postProcessGeneratedApi", "--stacktrace"))
        assertEquals(outputContent, File(outputDir, outputPath).readText())
    }

    @Suppress("SameParameterValue")
    private fun runBuild(projectDir: File, args: List<String>) {
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(args)
            .forwardOutput()
            .build()
    }

    private fun writeTestProject(inputDir: File, outputDir: File): File {
        val projectDir = File(tempDir, "project").also { it.mkdirs() }
        File(projectDir, "settings.gradle").writeText("")
        File(projectDir, "build.gradle").writeText(
            // language=groovy
            """
                import com.gabrielfeo.task.PostProcessGeneratedApi

                plugins {
                    id("com.gabrielfeo.no-op")
                }

                tasks.register("postProcessGeneratedApi", PostProcessGeneratedApi) {
                    originalFiles = new File("${inputDir.absolutePath}")
                    modelsPackage = "com.gabrielfeo.develocity.api.model"
                    postProcessedFiles = new File("${outputDir.absolutePath}")
                }
            """.trimIndent()
        )
        return projectDir
    }
}
