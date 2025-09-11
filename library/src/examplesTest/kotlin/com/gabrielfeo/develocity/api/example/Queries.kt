package com.gabrielfeo.develocity.api.example

object BuildStartTime {
    const val RECENT = "-10h"
}

object Queries {
    const val FAST = "buildStartTime>${BuildStartTime.RECENT} buildTool:gradle"
}