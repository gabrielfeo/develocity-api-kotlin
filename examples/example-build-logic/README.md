

# Example usage in build logic

This example shows how to create a reusable Gradle plugin that adds a `userBuildPerformanceMetrics` task to fetch and print build performance metrics for a specific user, using the Develocity API Kotlin client.

## Core files

- [`build-logic/src/main/kotlin/build/logic/PerformanceMetricsTask.kt`](./build-logic/src/main/kotlin/build/logic/PerformanceMetricsTask.kt): Implements a custom Gradle task that fetches and prints build performance metrics of a given user.
- [`build-logic/src/main/kotlin/build/logic/DevelocityApiService.kt`](./build-logic/src/main/kotlin/build/logic/DevelocityApiService.kt): Defines a shared build service containing the Develocity API client, which could be used by multiple tasks while ensuring a singleton client per build.
- [`build-logic/performance-metrics-plugin.gradle.kts`](./build-logic/performance-metrics-plugin.gradle.kts): A plugin which registers the task where it's applied.
- [`build.gradle.kts`](./build.gradle.kts): Applies `performance-metrics-plugin`, making the task available for this build.

## Usage

Run:

```sh
./gradlew userBuildPerformanceMetrics [--user=foo] --period=[-1d|-7d|-14d|-30d|...]
```
