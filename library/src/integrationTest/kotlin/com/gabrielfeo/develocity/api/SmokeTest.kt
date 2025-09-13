package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.RealEnv
import com.gabrielfeo.develocity.api.internal.asMap
import kotlin.test.Test
import kotlin.test.assertTrue

class SmokeTest {

    @Test
    fun testSuiteNotSensitiveToEnvironment() {
        assertTrue(RealEnv.asMap().isEmpty())
    }
}