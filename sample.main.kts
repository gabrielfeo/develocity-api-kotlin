@file:Repository("https://jitpack.io")
@file:DependsOn("com.github.gabrielfeo:gradle-enterprise-api-kotlin:0.4")

import com.gabrielfeo.gradle.enterprise.api.*

val builds = api.getBuilds(since = 0, maxBuilds = 3).execute().body()!!
builds.forEach {
    println(it)
}
