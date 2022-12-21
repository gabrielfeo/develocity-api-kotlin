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

Add a dependency by copying these lines to the top of your `kts` scripts:

```kotlin
@file:Repository("https://jitpack.io")
@file:DependsOn("com.github.gabrielfeo:gradle-enterprise-api-kotlin:1.0")
```

Following convention over configuration, set:
- the URL of your Gradle Enterprise instance, in environment variable `GRADLE_ENTERPRISE_URL`
- an API access token, either in macOS keychain labeled as "gradle-enterprise-api-token"
  or in an environment variable named `GRADLE_ENTERPRISE_API_TOKEN`

By doing so, any script can query `api` without additional setup. See the [sample
script](./sample.main.kts) for a complete example.

## Usage

The library provides API endpoints as a single [Retrofit][2] Service interface:
`GradleEnterpriseApi`. It's ready-to-use as the global `api` instance:

```kotlin
api.getBuild(id = "hy5nxbzfjxe5k")
```

It's recommended to learn about endpoints and their responses through IDE auto-complete. Javadoc
appearing in auto-complete is the full API manual. Each method is documented with
params and possible status codes.

Helper functions are also available:
- `GradleEnterpriseApi.buildsSequence()` returns a sequence making paged requests underneath

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
[API spec][5], using the [OpenAPI Generator Gradle Plugin][6]. Actual project classes are a thin
layer over the generated code, to make it easy to use from scripts.

### Custom setups

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

`api` is ready-to-use if conventions are followed, but if you'd rather you can set `baseUrl` and
`accessToken` from code instead, before using `api`:

```kotlin
baseUrl = "https://my.ge.org"
accessToken = "abcdefg"
api.getBuild(id = "hy5nxbzfjxe5k")
```

[1]: https://docs.gradle.com/enterprise/api-manual/
[2]: https://square.github.io/retrofit/
[3]: https://github.com/square/retrofit/issues/3448
[4]: https://github.com/square/retrofit/issues/3144#issuecomment-508300518
[5]: https://docs.gradle.com/enterprise/api-manual/#reference_documentation
[6]: https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc
