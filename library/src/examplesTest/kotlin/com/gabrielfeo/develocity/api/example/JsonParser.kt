package com.gabrielfeo.develocity.api.example

import com.squareup.moshi.Moshi
import okio.buffer
import okio.source
import java.nio.file.Path

object JsonAdapter {

    private val jsonAdapter = Moshi.Builder().build().adapter(Map::class.java)

    fun fromJson(path: Path): Map<*, *>? =
        jsonAdapter.fromJson(path.source().buffer())

    fun toJson(map: Map<*, *>?): String =
        jsonAdapter.toJson(map)

    fun toPrettyJson(map: Map<*, *>?): String =
        jsonAdapter.indent("  ").toJson(map)
}
