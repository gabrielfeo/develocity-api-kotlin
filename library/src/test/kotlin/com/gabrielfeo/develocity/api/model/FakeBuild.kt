package com.gabrielfeo.develocity.api.model

@Suppress("TestFunctionName")
fun FakeBuild(id: String, availableAt: Long) = Build(
    id = id,
    availableAt = availableAt,
    buildToolType = "",
    buildToolVersion = "",
    buildAgentVersion = "",
)
