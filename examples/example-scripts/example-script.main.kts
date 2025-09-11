#!/usr/bin/env kotlinc -script

/*
 * See what builds are most commonly invoked by developers, e.g. 'clean assemble',
 * 'test' or 'check'. You can set up the URL and a token for your Gradle
 * Enterprise instance and run this notebook as-is for your own project. This is a
 * simple example of something you can do with the API. It could bring insights,
 * for example:
 *
 * - "Our developers frequently clean together with assemble. We should ask them why,
 *   because they shouldn't have to. Just an old habit from Maven or are they working
 *   around a build issue we don't know about?"
 *
 * - "Some are doing check builds locally, which we set up to trigger our notably slow
 *   legacy tests. We should suggest they run test instead, leaving check for CI to run."
 *
 * Run this with at least 1GB of heap to accommodate the fetched data: JAVA_OPTS=-Xmx1g
 */

@file:DependsOn("com.gabrielfeo:develocity-api-kotlin:2024.3.0")

import com.gabrielfeo.develocity.api.*
import com.gabrielfeo.develocity.api.model.*
import com.gabrielfeo.develocity.api.extension.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.LinkedList

// Parameters
val query = args.getOrElse(0) { "buildStartTime>-7d buildTool:gradle" }

// Fetch builds from the API
val api = DevelocityApi.newInstance()
val builds: List<GradleAttributes> = runBlocking {
    api.buildsApi.getBuildsFlow(
        fromInstant = 0,
        query = query,
        models = listOf(BuildModelName.gradleAttributes),
    ).map {
        it.models!!.gradleAttributes!!.model!!
    }.toList(LinkedList())
}

// Process builds and count how many times each was invoked
check(builds.isNotEmpty()) { "No builds found. Adjust query and try again." }
val buildCounts = builds.groupBy { build ->
    val tasks = build.requestedTasks.joinToString(" ").trim(':')
    tasks.ifBlank { "IDE sync" }
}.mapValues { (_, builds) ->
    builds.size
}.entries.sortedByDescending { (_, count) ->
    count
}

// Print the top 5 as a pretty table
val table = buildCounts.take(5).joinToString("\n") { (tasks, count) ->
    "${tasks.padEnd(100)} | $count"
}
println(
    """
        |---------------------
        |Most frequent builds:
        |
        |$table
    """.trimMargin()
)

// Shutdown to end background threads and allow script to exit earlier (see README)
api.shutdown()
