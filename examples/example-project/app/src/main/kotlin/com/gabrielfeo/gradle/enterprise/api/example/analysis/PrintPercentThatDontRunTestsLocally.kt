package com.gabrielfeo.gradle.enterprise.api.example.analysis

import com.gabrielfeo.gradle.enterprise.api.GradleEnterpriseApi
import com.gabrielfeo.gradle.enterprise.api.getGradleAttributesFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import java.time.LocalDate
import java.time.ZoneOffset

suspend fun percentDevelopersThatDontRunTestsLocally(
    // Inject it so you can fake/mock it in tests if you want
    api: GradleEnterpriseApi,
): String {
    val oneMonthAgo = LocalDate.now()
        .minusMonths(1)
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()

    val buildsByUser = api.getGradleAttributesFlow(since = oneMonthAgo)
        .filter { "CI" !in it.tags }
        .toList()
        .groupBy { it.environment.username }

    check(buildsByUser.isNotEmpty()) { "No builds found!" }
    val userCount = buildsByUser.size
    val userCountDoesntRunTestsLocally = buildsByUser.count { (_, userBuilds) ->
        userBuilds.none { build ->
            build.requestedTasks.any { task -> "test" in task.lowercase() }
        }
    }

    val percent = "%.2f".format(userCountDoesntRunTestsLocally.toDouble() / userCount * 100)
    return "$percent% of developers don't run tests on their local machine"
}
