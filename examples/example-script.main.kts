@file:Repository("https://jitpack.io")
@file:DependsOn("com.github.gabrielfeo:gradle-enterprise-api-kotlin:0.14.0")

/*
 * Counts how many developers don't run tests on their local machine
 */

import com.gabrielfeo.gradle.enterprise.api.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.*

val oneMonthAgo = LocalDate.now()
    .minusMonths(1)
    .atStartOfDay()
    .toInstant(ZoneOffset.UTC)
    .toEpochMilli()

runBlocking {

    // Filter builds from the API
    val buildsByUser = gradleEnterpriseApi.getGradleAttributesFlow(since = oneMonthAgo)
        .filter { "CI" !in it.tags }
        .toList()
        .groupBy { it.environment.username }
    check(buildsByUser.isNotEmpty()) { "No builds found!" }

    // Count users
    val userCount = buildsByUser.size
    val userCountDoesntRunTestsLocally = buildsByUser.count { (_, userBuilds) ->
        userBuilds.none { build ->
            build.requestedTasks.any { task -> "test" in task.lowercase() }
        }
    }

    // Present result
    val percent = "%.2f".format(userCountDoesntRunTestsLocally / userCount.toDouble() * 100)
    print("$percent% of developers don't run tests on their local machine")
    shutdown()

}
