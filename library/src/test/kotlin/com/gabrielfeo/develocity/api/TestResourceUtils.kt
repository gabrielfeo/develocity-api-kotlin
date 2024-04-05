package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.infrastructure.Serializer
import com.squareup.moshi.adapter
import okio.buffer
import okio.source

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> readFromJsonResource(name: String): T {
    val adapter = Serializer.moshi.adapter<T>()
    val classLoader = T::class.java.classLoader
    val jsonSource = checkNotNull(classLoader.getResourceAsStream(name)).source().buffer()
    val obj = adapter.fromJson(jsonSource)
    return requireNotNull(obj) {
        "JSON resource $name is null"
    }
}
