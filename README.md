# Gradle Enterprise API Kotlin

[![Maven Central](https://img.shields.io/badge/Maven%20Central-2023.1.0-blue)][14]
[![Javadoc](https://img.shields.io/badge/Javadoc-2023.1.0-orange)][7]

A Kotlin library to access the [Gradle Enterprise API][1], easy to use from:

- [Jupyter notebooks with the Kotlin kernel][29]
- [Kotlin scripts (`kts`)][27]
- [Kotlin projects][28]

```kotlin
val api = GradleEnterpriseApi.newInstance()
api.buildsApi.getBuilds(since = yesterdayMilli).forEach {
  println(it)
}
```

 The library takes care of caching under the hood (opt-in) and provides some convenience extensions.

## Setup

Set up once and use the library from anywhere in your machine:

- [`GRADLE_ENTERPRISE_API_URL`][16] environment variable: the URL of your Gradle Enterprise instance
- [`GRADLE_ENTERPRISE_API_TOKEN`][17] environment variable: an API access token for the Gradle
  Enterprise instance.
  - Or a macOS keychain entry labeled `gradle-enterprise-api-token` (recommended).
  - <details>

      <summary>How to get an API token</summary>

      The Gradle Enterprise user must have the “Export build data via the API” permission.

      1. Sign in to Gradle Enterprise
      2. Go to "My settings" from the user menu in the top right-hand corner of the page
      3. Go to "Access keys" from the sidebar
      4. Click "Generate" on the right-hand side and copy the generated token.

    </details>

That's it! You can now use the library without any code configuration from notebooks, scripts or
projects.

### Setup snippets

ℹ️ The library is now published to Maven Central under `com.gabrielfeo`. Maven Central is
recommended over JitPack.

<details>
  <summary>Add to a Jupyter notebook</summary>

```
%useLatestDescriptors
%use gradle-enterprise-api-kotlin(version=2023.1.0)
```

</details>

<details>
  <summary>Add to a Kotlin script</summary>

```kotlin
@file:DependsOn("com.gabrielfeo:gradle-enterprise-api-kotlin:2023.1.0")
```

</details>

<details>
  <summary>Add to a Kotlin project</summary>

```kotlin
dependencies {
  implementation("com.gabrielfeo:gradle-enterprise-api-kotlin:2023.1.0")
}
```

</details>

## Usage

The [`GradleEnterpriseApi`][9] interface represents the Gradle Enterprise REST API. It contains
the 4 APIs exactly as listed in the [REST API Manual][5]:

```kotlin
interface GradleEnterpriseApi {
  val buildsApi: BuildsApi
  val buildCacheApi: BuildCacheApi
  val metaApi: MetaApi
  val testDistributionApi: TestDistributionApi
  // ...
}
```

For example, [`BuildsApi`][20] contains all endpoints under `/api/builds/`:

- [`BuildsApi.getBuilds`][21]: `GET /api/builds`
- [`BuildsApi.getGradleAttributes`][22]: `GET /api/builds/{id}/gradle-attributes`
- ...

### Calling the APIs

API methods are generated as suspend functions.
For most cases like scripts and notebooks, simply use [runBlocking][30]:

```kotlin
runBlocking {
  val builds: List<Build> = api.buildsApi.getBuilds(since = yesterdayMilli)
}
```

It's recommended to call [`GradleEnterpriseApi.shutdown()`][11] at the end of scripts to release
resources and let the program exit. Otherwise, it'll keep running for an extra ~60s after code
finishes, as an [expected behavior of OkHttp][4].

### Caching

HTTP caching is available, which can speed up queries significantly, but is
off by default. Enable by simply setting [`GRADLE_ENTERPRISE_API_CACHE_ENABLED`][12] to `true`. See
[`CacheConfig`][13] for caveats.

### Extensions

Explore the library's convenience extensions:
[`com.gabrielfeo.gradle.enterprise.api.extension`][25].

What you'll probably use the most is [`getGradleAttributesFlow`][24], which will call
`/api/builds` to get the list of build IDs since a given date and join each with
`/api/builds/{id}/gradle-attributes`, which contains tags and custom values on each build. It
also takes care of paging under-the-hood, returning a [`Flow`][26] of all builds since the given
date, so you don't have to worry about the REST API's limit of 1000 builds per request:

```kotlin
val builds: Flow<GradleAttributes> = api.buildsApi.getGradleAttributesFlow(since = lastYear)
builds.collect {
  // ...
}
```

## Documentation

[![Javadoc](https://img.shields.io/badge/javadoc-latest-orange)][7]

The javadoc of API interfaces and models, such as [`BuildsApi`][18] and [`GradleAttributes`][19],
matches the [REST API Manual][5] exactly. Both these classes and Gradle's own manual are generated
from the same OpenAPI spec.

## Optional setup

Creating a custom [`Config`][8] allows you to change library settings via code instead of
environment variables. It also lets you share resource between the library's `OkHttpClient` and
your own. For example:

```kotlin
val config = Config(
  apiUrl = "https://ge.mycompany.com/api/",
  apiToken = { vault.getGeApiToken() },
  clientBuilder = existingClient.newBuilder(),
)
val api = GradleEnterpriseApi.newInstance(config)
api.buildsApi.getBuilds(since = yesterdayMilli)
```

See the [`Config`][8] documentation for more.

## More info

- Currently built for Gradle Enterprise `2022.4`, but should work fine with previous and
  future versions. The library will be updated regularly for new API versions.
- Use JDK 8 or 14+ to run, if you want to avoid the ["illegal reflective access" warning about
  Retrofit][3]
- All classes live in these packages. If you need to make small edits to scripts where there's
  no auto-complete, wildcard imports can be used:

```kotlin
import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.model.*
import com.gabrielfeo.gradle.enterprise.api.model.extension.*
```

[1]: https://docs.gradle.com/enterprise/api-manual/
[2]: https://square.github.io/retrofit/
[3]: https://github.com/square/retrofit/issues/3448
[4]: https://github.com/square/retrofit/issues/3144#issuecomment-508300518
[5]: https://docs.gradle.com/enterprise/api-manual/ref/2022.4.html
[6]: https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc
[7]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/
[8]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-config/index.html
[9]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-gradle-enterprise-api/
[11]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-gradle-enterprise-api/shutdown.html
[12]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-config/-cache-config/cache-enabled.html
[13]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-config/-cache-config/index.html
[14]: https://central.sonatype.com/artifact/com.gabrielfeo/gradle-enterprise-api-kotlin/2023.1.0
[16]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-config/api-url.html
[17]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-config/api-token.html
[18]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-builds-api/index.html
[19]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api.model/-gradle-attributes/index.html
[20]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-builds-api/index.html
[21]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-builds-api/get-builds.html
[22]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-builds-api/get-gradle-attributes.html
[23]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api/-gradle-enterprise-api/-default-instance/index.html
[24]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api.extension/get-gradle-attributes-flow.html
[25]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/library/com.gabrielfeo.gradle.enterprise.api.extension/index.html
[26]: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/
[27]: ./examples/example-script.main.kts
[28]: ./examples/example-project
[29]: https://nbviewer.org/github/gabrielfeo/gradle-enterprise-api-kotlin/blob/main/examples/example-notebooks/MostFrequentBuilds.ipynb
[30]: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/run-blocking.html
