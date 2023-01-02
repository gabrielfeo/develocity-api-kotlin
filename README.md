# Gradle Enterprise API Kotlin

A Kotlin library to access the [Gradle Enterprise API][1], easy to use from Kotlin
scripts:

```kotlin
val builds = api.getBuilds(since = 0, maxBuilds = 10).execute().body()!!
builds.forEach {
  println(it)
}
```

## Setup

Set up your environment once and use the library from any script in your machine.

- `GRADLE_ENTERPRISE_URL` environment variable: the URL of your Gradle Enterprise instance
- `GRADLE_ENTERPRISE_API_TOKEN` environment variable: an API access token for the Gradle
  Enterprise instance. Alternatively, can be a macOS keychain entry labeled
  `gradle-enterprise-api-token` (recommended).

That's it! From any `kts` script, you can add a dependency on the library and start querying the
API:

```kotlin
@file:Repository("https://jitpack.io")
@file:DependsOn("com.github.gabrielfeo:gradle-enterprise-api-kotlin:1.0")

val builds = api.getBuilds(since = 0, maxBuilds = 10).execute().body()!!
builds.forEach {
  println(it)
}
```

See the [sample script](./sample.main.kts) for a complete example.

<details>
  <summary>Optional setup</summary>

  All of the following have default values and are completely optional. See
  [Api.kt](src/main/kotlin/com/gabrielfeo/gradle/enterprise/api/Api.kt) for details.

  ##### Caching

  Gradle Enterprise API disallows HTTP caching, but this library forces
  caching for faster queries. Caching is split in two categories:

  1. Short-term cache (default max-age of 1 day)
    - `/api/builds`
  2. Long-term cache (default max-age of 1 year)
    - `/api/builds/{id}/gradle-attributes`
    - `/api/builds/{id}/maven-attributes`

  max-age and cached URLs can be changed with options below.

  - `GRADLE_ENTERPRISE_API_CACHE_DIR`: HTTP cache location. Defaults to the system temporary
    directory.
  - `GRADLE_ENTERPRISE_API_MAX_CACHE_SIZE`: Max size of the HTTP cache in bytes. Defaults to ~1GB.
  - `GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_URL_PATTERN`: Regex pattern to match API URLs that are
    OK to store short-term in the HTTP cache.
  - `GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE`: Max age in seconds of each response stored
    short-term in the HTTP cache.
  - `GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_URL_PATTERN`: Regex pattern to match API URLs that are
    OK to store long-term in the HTTP cache.
  - `GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_MAX_AGE`: Max age in seconds of each response stored
    long-term in the HTTP cache.

  ##### Concurrency

  - `GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS`: Maximum amount of concurrent requests
    allowed. Defaults to 15.

  ##### Debugging

  - `GRADLE_ENTERPRISE_API_DEBUG_LOGGING`: `true` to enable debug logging from the library. Defaults
    to `false`.

</details>

<details>
  <summary>Setup in full projects (non-scripts)</summary>

  You can also use it in a full Kotlin project instead of a script. Just add a dependency:

  ```kotlin
  repositories {
    maven(url = "https://jitpack.io")
  }
  dependencies {
    implementation("com.github.gabrielfeo:gradle-enterprise-api-kotlin:1.0")
  }
  ```

  <details>
    <summary>Groovy</summary>

    ```groovy
    repositories {
      maven { url = 'https://jitpack.io' }
    }
    dependencies {
      implementation 'com.github.gabrielfeo:gradle-enterprise-api-kotlin:1.0'
    }
    ```

  </details>

  Any option can also be changed from code rather than from environment, as long as it's done
  before the first `api` usage.

  ```kotlin
  baseUrl = { "https://my.ge.org" }
  accessToken = { getFromVault("ge-api-token") }
  api.getBuilds(id = "hy5nxbzfjxe5k")
  ```

</details>

## Usage

API endpoints are provided as a single interface: `GradleEnterpriseApi`. It's
initialized and ready-to-use as the global `api` instance:

```kotlin
api.getBuild(id = "hy5nxbzfjxe5k")
```

It's recommended to learn about endpoints and their responses through IDE auto-complete. Javadoc
appearing in auto-complete is the full API manual, same as Gradle's online docs.

This library provides a few helper functions on top of the regular API:

```kotlin
// Regular query to /api/builds, limited to 1000 builds server-side
api.getBuilds(since = lastMonth)
// Streams all available builds from a given date, split in as getBuilds
// as needed
api.getBuildsFlow(since = lastMonth)
```

```kotlin
// To get build scan data such as username, tags and custom values, one
// must usually query /api/builds/{id}/gradle-attributes per-build, which
// is verbose and slow (1 request at a time)
api.getBuildsFlow(since = lastMonth)
  .map { build -> api.getGradleAttributes(id = build.id) }
// Streams all available builds as GradleAttributes from a given date,
// requesting more than 1 build at a time.
api.getGradleAttributesFlow(since = lastMonth)
```


## More info

- Currently built for Gradle Enterprise `2022.4`, but can be used with previous versions.
- Use JDK 8 or 14+ to run, if you want to avoid the ["illegal reflective access" warning about
  Retrofit][3]
- There is a global instance `okHttpClient` so you can change what's needed, but also concurrency
  shortcuts `maxConcurrentRequests` and `shutdown()`.
  - `maxConcurrentRequests` is useful to speed up scripts, but if you start getting HTTP 504 from
    your GE instance, decreasing this value should help.
  - The script will keep running for an extra ~60s after code finishes, as an [expected behavior
  of OkHttp][4], unless you call `shutdown()` (global function).
- All classes are in the same package, so that if you need to make small edits to scripts where
  there's no auto-complete, a single wildcard import can be used:

```kotlin
import com.gabrielfeo.gradle.enterprise.api.*
```

###  Internals

API classes such as `GradleEnterpriseApi` and response models are generated from the offical
[API spec][5], using the [OpenAPI Generator Gradle Plugin][6].

[1]: https://docs.gradle.com/enterprise/api-manual/
[2]: https://square.github.io/retrofit/
[3]: https://github.com/square/retrofit/issues/3448
[4]: https://github.com/square/retrofit/issues/3144#issuecomment-508300518
[5]: https://docs.gradle.com/enterprise/api-manual/#reference_documentation
[6]: https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc
