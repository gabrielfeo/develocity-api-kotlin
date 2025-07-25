package com.gabrielfeo.develocity.api.example.notebook

class NotebookJson(
    val properties: Map<String, Any?>,
) {

    val cells = properties["cells"] as List<Map<String, Any>>

    val allOutputs by lazy {
        cells.flatMap { (it["outputs"] as? List<Map<String, Any>>).orEmpty() }
    }

    val textOutputLines by lazy {
        allOutputs
            .filter { it["output_type"] == "stream" }
            .flatMap { it["text"] as List<String> }
    }

    val dataframeOutputs by lazy {
        executeOutputsByMimeType
            .filter { (mimeType, _) -> mimeType == "application/kotlindataframe+json" }
            .map { it.value as String }
    }

    private val executeOutputsByMimeType by lazy {
        allOutputs
            .filter { it["output_type"] == "execute_result" }
            .flatMap { (it["data"] as Map<String, Any>).entries }
    }

    val kandyOutputs by lazy {
        executeOutputsByMimeType
            .filter { (mimeType, _) -> mimeType == "application/plot+json" }
            .map { it.value as Map<String, Any> }
    }
}

fun Map<*, *>?.asNotebookJson() = NotebookJson(this as Map<String, Any?>)
