package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.RealEnv
import com.gabrielfeo.develocity.api.internal.asMap
import kotlin.test.Test

class SmokeTest {

    @Test
    fun testSuiteNotSensitiveToEnvironment() {
        RealEnv.asMap().isEmpty()
    }
}